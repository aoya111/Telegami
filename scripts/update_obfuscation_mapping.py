#!/usr/bin/env python3
"""Generate and validate obfuscation mappings for Telegami.

The source of truth is an R8 `mapping.txt` from the target app build. This
script extracts every Telegram class/method/field reference used by the
module's `virt/` and `hooks/` layers, resolves it through the R8 mapping, and
merges the result into the existing variant JSON.

When a method name has several overloads, the script uses the descriptor that
the existing JSON already selected (derived from `--old-mapping`) or the
member `hint`, so the correct overload is chosen.

R8 mapping format notes:
  * class:       `original.Class -> obfuscated.Class:`
  * field:       `    <type> <name> -> <obfuscated>`
  * method:      `    <type> <name>(<args>) -> <obfuscated>`
  * inlined:     `    <start>:<end>:<type> [Class.]<name>(<args>):<a>[:<b>] -> <obfuscated>`
The `->` value on an inlined line is the obfuscated name of the method named on
the left (not the caller).
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from dataclasses import dataclass, field
from pathlib import Path

CLASS_LINE = re.compile(r"^(?P<orig>\S+) -> (?P<obf>\S+):$")
PLAIN_MEMBER = re.compile(r"^    (?P<ret>\S+) (?P<name>[^(\s]+)(?P<args>\(.*\))? -> (?P<obf>\S+)$")
FRAME = re.compile(
    r"^\s*\d+:\d+:(?P<ret>\S+) (?:(?P<cls>[^\s.]+)\.)?"
    r"(?P<name>[^(\s]+)\((?P<args>.*?)\)(?::[\d:]+)? -> (?P<obf>\S+)$"
)
ASSIGN = re.compile(
    r"(?:const\s+)?val\s+(?P<var>[A-Za-z_][A-Za-z0-9_]*)\s*(?::\s*String)?\s*=\s*(?P<expr>.+)$"
)
GET_CALL = re.compile(r"(?:resolver|ResolverManager)\.get\(\s*(?P<expr>[^)]+?)\s*\)")
MEMBER_CALL = re.compile(
    r"resolver\.(?P<kind>getMethod|getField)\(\s*(?P<expr>[^,]+?)\s*,\s*\"(?P<member>[^\"]+)\"\s*\)"
)
FIND_METHOD_CALL = re.compile(r"findMethod\(\s*(?P<expr>[^,]+?)\s*,\s*\"(?P<member>[^\"]+)\"")
CLASSOF_CALL = re.compile(r"classOf\(\s*\"(?P<literal>[^\"]+)\"\s*\)")
STRING_LITERAL = re.compile(r'"(?P<value>(?:\\.|[^"\\])*)"')
TG_CLASS_LITERAL = re.compile(r"^org\.telegram\.(?:[a-z0-9_]+\.)+[A-Z][A-Za-z0-9_]*$")
TG_CLASS_LITERAL_NESTED = re.compile(r"^org\.telegram\..+\$[A-Za-z0-9_$]+$")

MEMBER_OWNER = {
    "org.telegram.ui.ActionBar.AlertDialog": "org.telegram.ui.ActionBar.AlertDialog$Builder",
}

# JSON keys use semantic names in a few places; map them to the real member
# names so the R8 mapping can be looked up.
MEMBER_ALIASES = {
    "org.telegram.ui.ActionBar.Theme": {"fields": {"activeTheme": "currentTheme"}},
    "org.telegram.ui.SecretMediaViewer": {
        "fields": {"activeViewer": "Instance", "messageObject": "currentMessageObject"},
    },
}

SKIP_CLASS_MARKERS = ("$$ExternalSynthetic", "$r8$")
# Hardcoded inline hosts for other clients; they belong in their own variant file.
IGNORE_CLASSES = {"org.telegram.ui.ao", "org.telegram.ui.u"}


@dataclass(frozen=True)
class MethodInfo:
    args: tuple[str, ...]
    obfuscated: str


@dataclass
class ClassInfo:
    obfuscated: str
    methods: dict[str, list[MethodInfo]] = field(default_factory=dict)
    fields: dict[str, str] = field(default_factory=dict)


def unquote(value: str) -> str:
    value = value.strip()
    if len(value) >= 2 and value[0] == '"' and value[-1] == '"':
        value = value[1:-1]
    return value.replace('\\$', '$')


def split_args(raw: str) -> tuple[str, ...]:
    raw = raw.strip()
    if not raw:
        return ()
    return tuple(part.strip() for part in raw.split(",") if part.strip())


def parse_mapping(path: Path) -> tuple[dict[str, ClassInfo], dict[str, list[str]]]:
    classes: dict[str, ClassInfo] = {}
    simple_index: dict[str, list[str]] = {}
    current: str | None = None

    def add_method(owner: str, name: str, args: tuple[str, ...], obf: str) -> None:
        info = classes.setdefault(owner, ClassInfo(obfuscated=owner))
        existing = info.methods.setdefault(name, [])
        if (args, obf) not in {(m.args, m.obfuscated) for m in existing}:
            existing.append(MethodInfo(args=args, obfuscated=obf))

    with path.open("r", errors="replace") as handle:
        for line in handle:
            line = line.rstrip("\n")
            if not line or line.startswith("#"):
                continue

            class_match = CLASS_LINE.match(line)
            if class_match:
                current = class_match.group("orig")
                old = classes.get(current)
                classes[current] = ClassInfo(obfuscated=class_match.group("obf"))
                if old is not None:
                    classes[current].methods = old.methods
                    classes[current].fields = old.fields
                simple = current.rsplit(".", 1)[-1]
                simple_index.setdefault(simple, []).append(current)
                continue

            if current is None:
                continue

            member = PLAIN_MEMBER.match(line)
            if member and not member.group("args"):
                classes[current].fields[member.group("name")] = member.group("obf")
                continue

            frame = FRAME.match(line)
            if frame:
                declared = frame.group("cls")
                if declared:
                    candidates = simple_index.get(declared, [])
                    owner = candidates[0] if len(candidates) == 1 else declared
                else:
                    owner = current
                add_method(owner, frame.group("name"), split_args(frame.group("args")), frame.group("obf"))
                continue

            member = PLAIN_MEMBER.match(line)
            if member and member.group("args"):
                args = split_args(member.group("args")[1:-1])
                add_method(current, member.group("name"), args, member.group("obf"))

    return classes, simple_index


def assignments_for(lines: list[str]) -> list[tuple[int, str, str]]:
    result = []
    for index, line in enumerate(lines):
        match = ASSIGN.search(line)
        if match:
            result.append((index, match.group("var"), match.group("expr").strip()))
    return result


def resolve_expr(
    expr: str,
    line_no: int,
    assignments: list[tuple[int, str, str]],
    file_value: str | None,
    seen: set[str] | None = None,
) -> str | None:
    expr = expr.strip()
    seen = seen or set()

    if expr.startswith('"'):
        value = unquote(expr)
        return value.replace("$alertDialogName", "org.telegram.ui.ActionBar.AlertDialog")

    if re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*", expr):
        if expr == "alertDialogName":
            return "org.telegram.ui.ActionBar.AlertDialog"
        if expr in seen:
            return None
        candidates = [a for a in assignments if a[0] <= line_no and a[1] == expr]
        chosen = candidates[-1] if candidates else next((a for a in assignments if a[1] == expr), None)
        if chosen is None:
            return None
        return resolve_expr(chosen[2], chosen[0], assignments, file_value, seen | {expr})

    if expr.startswith("$"):
        return None
    return unquote(expr)


def extract_refs(repo: Path) -> dict[str, dict[str, set[str]]]:
    refs: dict[str, dict[str, set[str]]] = {}

    def bucket(klass: str) -> dict[str, set[str]]:
        return refs.setdefault(klass, {"methods": set(), "fields": set()})

    roots = [repo / "app/src/main/java/com/aoya/telegami/virt", repo / "app/src/main/java/com/aoya/telegami/hooks"]
    for root in roots:
        for path in sorted(root.rglob("*.kt")):
            text = path.read_text(errors="replace")
            lines = text.splitlines()
            assignments = assignments_for(lines)
            file_value = None
            for _, var, expr in assignments:
                if var == "OBJ_PATH" and expr.startswith('"'):
                    file_value = unquote(expr)
                    break

            for index, line in enumerate(lines):
                for match in MEMBER_CALL.finditer(line):
                    klass = resolve_expr(match.group("expr"), index, assignments, file_value)
                    if not klass:
                        continue
                    key = "methods" if match.group("kind") == "getMethod" else "fields"
                    bucket(klass)[key].add(match.group("member"))

                for match in GET_CALL.finditer(line):
                    klass = resolve_expr(match.group("expr"), index, assignments, file_value)
                    if klass and klass.startswith("org.telegram."):
                        bucket(klass)

                for match in FIND_METHOD_CALL.finditer(line):
                    raw = match.group("expr")
                    if "(" in raw or "resolver." in raw:
                        continue
                    klass = resolve_expr(raw, index, assignments, file_value)
                    if klass and klass.startswith("org.telegram."):
                        bucket(klass)["methods"].add(match.group("member"))

                for match in CLASSOF_CALL.finditer(line):
                    literal = unquote(match.group("literal"))
                    if literal.startswith("org.telegram."):
                        bucket(literal)

                for match in STRING_LITERAL.finditer(line):
                    literal = unquote(match.group("value"))
                    if TG_CLASS_LITERAL.match(literal) or TG_CLASS_LITERAL_NESTED.match(literal):
                        bucket(literal)

            # AlertDialog.kt loads the nested Builder by literal name.
            if path.name == "AlertDialog.kt":
                bucket("org.telegram.ui.ActionBar.AlertDialog$Builder")

    return refs


def simple_type(value: str) -> str:
    value = value.strip()
    value = value.replace("<", "").replace(">", "")
    value = value.replace("...", "[]")
    value = re.sub(r"[^A-Za-z0-9_$\[\]]", "", value)
    value = value.split(".")[-1].split("$")[-1]
    return value.lower()


def hint_types(hint: str | None) -> tuple[str, ...] | None:
    if not hint:
        return None
    text = hint.strip()
    match = re.search(r"\(([^)]*)\)", text)
    if match:
        raw = match.group(1)
    elif "," in text:
        raw = text
    elif re.fullmatch(r"[<A-Za-z0-9_$.\[\]]+", text):
        raw = text
    else:
        return None

    parts = []
    for part in raw.split(","):
        part = re.sub(r"\b(final|public|private|protected|static)\b", "", part).strip()
        if not part:
            continue
        parts.append(part.split()[0])
    return tuple(parts)


def matches_types(candidate_args: tuple[str, ...], wanted: tuple[str, ...]) -> bool:
    if len(candidate_args) != len(wanted):
        return False
    return all(simple_type(a) == simple_type(b) for a, b in zip(candidate_args, wanted))


def pick_method(
    key: str,
    name: str,
    candidates: list[MethodInfo],
    hint: str | None,
    desired: tuple[str, ...] | None,
) -> tuple[MethodInfo | None, str | None]:
    if not candidates:
        return None, None
    if len(candidates) == 1:
        return candidates[0], None

    if desired is not None:
        filtered = [c for c in candidates if c.args == desired]
        if len(filtered) == 1:
            return filtered[0], None
        if len(filtered) > 1:
            return filtered[0], f"ambiguous overloads for {key}.{name}"

    wanted = hint_types(hint)
    if wanted is not None:
        filtered = [c for c in candidates if matches_types(c.args, wanted)]
        if len(filtered) == 1:
            return filtered[0], None
        if len(filtered) > 1:
            return filtered[0], f"ambiguous overloads for {key}.{name} (hint)"

    return candidates[0], f"ambiguous overloads for {key}.{name} (using first)"


def desired_descriptors(existing: dict, old_mapping: dict[str, ClassInfo]) -> dict[tuple[str, str, str], tuple[str, ...]]:
    result: dict[tuple[str, str, str], tuple[str, ...]] = {}
    for key, entry in existing.get("mappings", {}).items():
        owner = MEMBER_OWNER.get(key, key)
        info = old_mapping.get(owner)
        if info is None:
            continue
        aliases = MEMBER_ALIASES.get(key, {})
        for kind in ("methods",):
            for name, item in entry.get(kind, {}).items():
                real = aliases.get(kind, {}).get(name, name)
                matches = [c for c in info.methods.get(real, []) if c.obfuscated == item["obfuscated"]]
                if len(matches) == 1:
                    result[(key, kind, name)] = matches[0].args
    return result


def merge_mapping(
    existing: dict,
    mapping: dict[str, ClassInfo],
    refs: dict[str, dict[str, set[str]]],
    desired: dict[tuple[str, str, str], tuple[str, ...]],
    overrides: dict | None = None,
    report: list[dict] | None = None,
) -> tuple[dict, list[str]]:
    overrides = overrides or {}
    report = report if report is not None else []
    warnings: list[str] = []
    out = {
        "variant": existing.get("variant", "nekogram"),
        "package_name": existing.get("package_name", ""),
        "version": existing.get("version"),
        "updated": existing.get("updated"),
        "mappings": {},
    }

    keys = {
        key
        for key in set(existing.get("mappings", {})) | set(refs) | set(overrides)
        if key.startswith(("org.telegram.", "tw.nekomimi.")) and not any(marker in key for marker in SKIP_CLASS_MARKERS)
        and key not in IGNORE_CLASSES
    }

    for key in sorted(keys):
        entry = existing.get("mappings", {}).get(key, {})
        owner = MEMBER_OWNER.get(key, key)
        info = mapping.get(owner)
        aliases = MEMBER_ALIASES.get(key, {})

        methods = {}
        wanted_methods = (
            set(entry.get("methods", {}))
            | refs.get(key, {}).get("methods", set())
            | set(overrides.get(key, {}).get("methods", {}))
        )
        for name in sorted(wanted_methods):
            real = aliases.get("methods", {}).get(name, name)
            hint = entry.get("methods", {}).get(name, {}).get("hint")
            candidates = info.methods.get(real, []) if info else []
            chosen, warning = pick_method(key, name, candidates, hint, desired.get((key, "methods", name)))
            if warning:
                warnings.append(warning)
            override = overrides.get(key, {}).get("methods", {}).get(name)
            if override is not None:
                obf = override
            elif chosen is None:
                obf = real
                if info is not None:
                    warnings.append(f"METHOD NOT RENAMED: {key}.{name} (using {real})")
            else:
                obf = chosen.obfuscated
            if warning or override is not None or chosen is None:
                report.append(
                    {
                        "class": key,
                        "member": name,
                        "kind": "method",
                        "hint": hint,
                        "chosen": obf,
                        "candidates": [{"obfuscated": c.obfuscated, "args": list(c.args)} for c in candidates],
                    }
                )
            methods[name] = {"hint": hint, "obfuscated": obf}

        fields = {}
        wanted_fields = (
            set(entry.get("fields", {}))
            | refs.get(key, {}).get("fields", set())
            | set(overrides.get(key, {}).get("fields", {}))
        )
        for name in sorted(wanted_fields):
            real = aliases.get("fields", {}).get(name, name)
            obf = info.fields.get(real) if info else None
            override = overrides.get(key, {}).get("fields", {}).get(name)
            if override is not None:
                obf = override
            if obf is None:
                obf = real
                if info is not None:
                    warnings.append(f"FIELD NOT RENAMED: {key}.{name} (using {real})")
            if obf is None or override is not None or (info is not None and real not in info.fields):
                report.append(
                    {
                        "class": key,
                        "member": name,
                        "kind": "field",
                        "hint": entry.get("fields", {}).get(name, {}).get("hint"),
                        "chosen": obf,
                        "candidates": [],
                    }
                )
            fields[name] = {
                "hint": entry.get("fields", {}).get(name, {}).get("hint"),
                "obfuscated": obf,
            }

        out["mappings"][key] = {
            "fields": fields,
            "hint": entry.get("hint"),
            "methods": methods,
            "obfuscated": overrides.get(key, {}).get("obfuscated")
            or (mapping[key].obfuscated if key in mapping else key),
        }

    return out, warnings


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--mapping", type=Path, required=True, help="target R8 mapping.txt")
    parser.add_argument("--old-mapping", type=Path, help="previous R8 mapping.txt (for overload selection)")
    parser.add_argument("--existing", type=Path, required=True, help="existing variant JSON")
    parser.add_argument("--repo", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--output", type=Path, help="write merged JSON here (default: dry run)")
    parser.add_argument("--overrides", type=Path, help="JSON overrides: class -> methods/fields")
    parser.add_argument("--report", type=Path, help="write unresolved/ambiguous entries as JSON")
    parser.add_argument("--version", help="override version field")
    parser.add_argument("--updated", help="override updated field")
    args = parser.parse_args()

    existing = json.loads(args.existing.read_text())
    mapping, _ = parse_mapping(args.mapping)
    refs = extract_refs(args.repo)
    overrides = json.loads(args.overrides.read_text()) if args.overrides else {}

    desired: dict[tuple[str, str, str], tuple[str, ...]] = {}
    if args.old_mapping:
        old_mapping, _ = parse_mapping(args.old_mapping)
        desired = desired_descriptors(existing, old_mapping)

    report: list[dict] = []
    merged, warnings = merge_mapping(existing, mapping, refs, desired, overrides, report)
    if args.version:
        merged["version"] = args.version
    if args.updated:
        merged["updated"] = args.updated

    for warning in warnings:
        print(warning, file=sys.stderr)
    print(f"references: {len(refs)} classes; mapping: {len(mapping)} classes; warnings: {len(warnings)}")

    if args.report:
        args.report.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n")
        print(f"wrote report {args.report} ({len(report)} entries)")

    if args.output:
        args.output.write_text(json.dumps(merged, indent=2, ensure_ascii=False) + "\n")
        print(f"wrote {args.output}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
