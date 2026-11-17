#!/usr/bin/env python3
"""Audit an obfuscation mapping against JADX Java sources.

This deliberately reports candidates instead of rewriting mappings: a string
or signature can identify more than one member after an app update.
"""

import argparse
import json
import re
import sys
from pathlib import Path


def source_path(sources: Path, class_name: str) -> Path:
    """Return the JADX file containing a top-level or nested Java class."""
    if "$" in class_name:
        # R8 can keep a nested class name while renaming/removing the outer one,
        # in which case JADX writes a standalone `Outer$Inner.java` file.
        separate = sources / (class_name.replace(".", "/") + ".java")
        if separate.is_file():
            return separate
    top_level = class_name.split("$")[0]
    if "." not in top_level:
        return sources / "defpackage" / f"{top_level}.java"
    return sources / (top_level.replace(".", "/") + ".java")


# Entries whose members are actually declared on a different R8 class.
MEMBER_OWNER = {
    "org.telegram.ui.ActionBar.AlertDialog": "org.telegram.ui.ActionBar.AlertDialog$Builder",
}


def has_method(source: str, name: str) -> bool:
    declaration = re.compile(
        rf"(?m)^\s*(?:public|protected|private|static|final|synchronized|native|"
        rf"abstract|\s)+[^;{{}}()=]+\b{re.escape(name)}\s*\(",
    )
    if declaration.search(source) is not None:
        return True

    # JADX renames colliding members (for example, `C` to `x`) and preserves
    # the dex name in a comment. Xposed must use the original dex name.
    renamed = re.compile(rf"/\* renamed from: {re.escape(name)}(?:,| \*/)")
    return renamed.search(source) is not None


def has_class(source: str, class_name: str) -> bool:
    """Check the nested declaration when JADX stores it in an outer file."""
    if "$" not in class_name:
        return True
    simple = class_name.rsplit("$", maxsplit=1)[1]
    full = class_name.rsplit(".", maxsplit=1)[-1]
    names = {simple, full}
    declaration = re.compile(
        r"\b(?:class|interface|enum)\s+(?:" + "|".join(re.escape(name) for name in names) + r")\b",
    )
    return declaration.search(source) is not None


def has_field(source: str, name: str) -> bool:
    declaration = re.compile(
        rf"(?m)^\s*(?:public|protected|private|static|final|volatile|transient|\s)+"
        rf"[^;{{}}()=]+\b{re.escape(name)}\s*(?:=|;|,)",
    )
    return declaration.search(source) is not None


def hinted_method_names(source: str, hint: str) -> set[str]:
    """Return dex names for declarations matched by a signature-like hint."""
    if not any(token in hint for token in ("public", "final", "static")):
        return set()
    try:
        pattern = re.compile(hint)
    except re.error:
        # Older mappings use regex tokens such as \w* but leave the method's
        # opening parenthesis unescaped. Preserve the tokens and repair that
        # declaration punctuation for signature matching.
        try:
            pattern = re.compile(re.sub(r"(?<!\\)\(", r"\\(", hint))
        except re.error:
            return set()

    names = set()
    declaration = re.compile(
        r"(?m)^\s*public\s+(?:static\s+)?(?:final\s+)?[^;{}()=]+\b(?P<name>\w+)\s*\(",
    )
    for match in declaration.finditer(source):
        if not pattern.search(match.group(0)):
            continue
        prefix = source[max(0, match.start() - 300) : match.start()]
        renamed = re.search(r"/\* renamed from: (?P<name>\w+)(?:,| \*/)", prefix)
        names.add(renamed["name"] if renamed else match["name"])
    return names


def collect_hint_matches(mapping: dict, sources: Path, limit: int) -> dict[str, list[Path]]:
    """Search every source once, avoiding one full tree traversal per hint."""
    patterns = {}
    for original, entry in mapping["mappings"].items():
        hint = entry.get("hint")
        if not hint:
            continue
        try:
            patterns[original] = re.compile(hint)
        except re.error as error:
            # Existing mappings also use plain text snippets containing `(`.
            # Treat malformed regexes as literals rather than dropping them.
            print(f"LITERAL HINT {original}: invalid regex ({error})")
            patterns[original] = re.compile(re.escape(hint))

    matches = {original: [] for original in patterns}
    for path in sources.rglob("*.java"):
        try:
            source = path.read_text(errors="replace")
        except OSError as error:
            print(f"  could not read {path}: {error}", file=sys.stderr)
            continue
        for original, pattern in patterns.items():
            if len(matches[original]) < limit and pattern.search(source):
                matches[original].append(path.relative_to(sources))
    return matches


def audit(mapping: dict, sources: Path, search_hints: bool, limit: int) -> int:
    problems = 0
    hint_matches = collect_hint_matches(mapping, sources, limit) if search_hints else {}
    for original, entry in mapping["mappings"].items():
        obfuscated = entry["obfuscated"]
        path = source_path(sources, obfuscated)
        if not path.is_file():
            print(f"MISSING CLASS  {original} -> {obfuscated} ({path})")
            problems += 1
            continue

        source = path.read_text(errors="replace")
        if not has_class(source, obfuscated):
            print(f"MISSING CLASS  {original} -> {obfuscated} ({path})")
            problems += 1
            continue

        owner_key = MEMBER_OWNER.get(original, original)
        if owner_key != original:
            owner_entry = mapping["mappings"].get(owner_key)
            owner_obf = owner_entry["obfuscated"] if owner_entry else owner_key
            member_path = source_path(sources, owner_obf)
            member_source = member_path.read_text(errors="replace") if member_path.is_file() else source
        else:
            member_path = path
            member_source = source

        for original_method, method in entry.get("methods", {}).items():
            if not has_method(member_source, method["obfuscated"]):
                print(f"MISSING METHOD {original}.{original_method} -> {method['obfuscated']} ({member_path})")
                problems += 1
                continue

            hinted_names = hinted_method_names(member_source, method.get("hint") or "")
            if len(hinted_names) == 1 and method["obfuscated"] not in hinted_names:
                expected = next(iter(hinted_names))
                print(
                    "SIGNATURE MISMATCH "
                    f"{original}.{original_method} -> {method['obfuscated']} "
                    f"(hint resolves to {expected}; {member_path})"
                )
                problems += 1

        for original_field, field in entry.get("fields", {}).items():
            if not has_field(member_source, field["obfuscated"]):
                print(f"MISSING FIELD  {original}.{original_field} -> {field['obfuscated']} ({member_path})")
                problems += 1

        if search_hints and entry.get("hint"):
            matches = hint_matches.get(original, [])
            rendered = ", ".join(str(match) for match in matches) or "none"
            print(f"HINT {original}: {rendered}")

    return problems


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("mapping", type=Path, help="Mapping JSON to audit")
    parser.add_argument("sources", type=Path, help="JADX sources directory")
    parser.add_argument("--search-hints", action="store_true", help="List source files matching class hints")
    parser.add_argument("--hint-limit", type=int, default=3, help="Candidates per class hint (default: 3)")
    args = parser.parse_args()

    if not args.sources.is_dir():
        parser.error(f"sources directory does not exist: {args.sources}")
    if args.hint_limit < 1:
        parser.error("--hint-limit must be positive")

    with args.mapping.open() as file:
        mapping = json.load(file)
    if not isinstance(mapping.get("mappings"), dict):
        parser.error("mapping must contain a mappings object")

    problems = audit(mapping, args.sources, args.search_hints, args.hint_limit)
    print(f"Audited {len(mapping['mappings'])} classes; {problems} stale entries found.")
    return 1 if problems else 0


if __name__ == "__main__":
    raise SystemExit(main())
