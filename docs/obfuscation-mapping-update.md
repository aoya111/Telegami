# Updating an Obfuscation Mapping (`nekogram.json`)

This document describes the workflow used to rebuild `nekogram.json` for
Nekogram 13.2.2. It complements [obfuscation.md](obfuscation.md), which explains
the JSON structure itself.

The same workflow applies to any variant, but **requires an R8
`mapping.txt` from the exact build**. Decompiled sources alone are not enough:
JADX only shows the obfuscated names.

---

## 1. Inputs

You need four things:

1. **Target APK** (for device testing and JADX)
2. **R8 `mapping.txt`** from the same build
3. **Decompiled JADX sources** for the same APK
4. **Previous mapping JSON** and/or an overrides file

Example for Nekogram 13.2.2:

```text
APK:      Nekogram-13.2.2-7303-arm64-v8a.apk
Mapping:  mapping/release/mapping.txt  (from the CI release artifact)
Sources:  /path/to/nekogram-13.2.2/sources
JSON:     app/src/main/assets/obfuscation_mappings/nekogram.json
```

Nekogram CI publishes the R8 mapping in the release build artifact, usually as
`mapping/release/mapping.txt`. Download and unzip it before generating anything.

If you do **not** have `mapping.txt`, stop here. Manually matching obfuscated
members from JADX is possible but slow and version-fragile.

---

## 2. Tools

### `scripts/update_obfuscation_mapping.py`

This is the generator.

What it does:

1. Parses the R8 `mapping.txt`.
2. Scans `app/src/main/java/com/aoya/telegami/virt` and `hooks` for references
   such as:

   ```kotlin
   resolver.get("org.telegram.ui.ChatActivity")
   resolver.getMethod("org.telegram.messenger.MessagesController", "getUser")
   resolver.getField("org.telegram.ui.ProfileActivity", "usernameRow")
   classLoader.findMethod("org.telegram.ui.ChatActivity", "fillMessageMenu")
   ```

3. Resolves each reference through the R8 mapping.
4. Merges the result into the existing JSON.
5. Applies explicit overrides from an overrides file.
6. Reports unresolved/ambiguous members.

Usage:

```bash
python3 scripts/update_obfuscation_mapping.py \
  --mapping      /path/to/new/mapping.txt \
  --old-mapping  /path/to/old/mapping.txt \
  --existing     app/src/main/assets/obfuscation_mappings/nekogram.json \
  --overrides    /path/to/overrides.json \
  --output       /tmp/nekogram-new.json \
  --report       /tmp/nekogram-report.json \
  --version      13.2.2 \
  --updated      2026-11-15
```

Then inspect and copy:

```bash
cp /tmp/nekogram-new.json \
   app/src/main/assets/obfuscation_mappings/nekogram.json
```

### `scripts/audit_obfuscation_mapping.py`

Checks an existing JSON against JADX sources and reports stale entries:

```bash
python3 scripts/audit_obfuscation_mapping.py \
  app/src/main/assets/obfuscation_mappings/nekogram.json \
  /path/to/jadx/sources
```

This is useful before and after an update. It prints entries like:

```text
MISSING METHOD org.telegram.ui.ProfileActivity.updateProfileData -> i5
MISSING FIELD  org.telegram.ui.ProfileActivity.usernameRow -> K3
```

---

## 3. Overrides file

The generator can usually resolve normal class/method/field renames. Overrides
are for cases where automatic resolution is wrong, ambiguous, or impossible:
merged classes, synthetic lambdas, removed methods, and semantic aliases.

Format:

```json
{
  "org.telegram.ui.ProfileActivity": {
    "methods": {
      "processOnClickOrPress": "z3",
      "updateOnlineText": "R4"
    },
    "fields": {
      "usernameRow": "E4"
    }
  },
  "org.telegram.tgnet.TLRPC$Chat": {
    "fields": {
      "username": "w",
      "noforwards": "F"
    }
  }
}
```

You can also alias a synthetic class:

```json
{
  "org.telegram.messenger.MessagesStorage$emptyMediaLambda": {
    "obfuscated": "m77",
    "fields": {
      "type": "c",
      "dialogId": "n",
      "ids": "h"
    }
  }
}
```

Aliases are useful because the generator skips `$$ExternalSynthetic...` class
names. Use a semantic alias instead, and reference it from code:

```kotlin
val lambdaClass = classLoader.loadClass(
    resolver.get("org.telegram.messenger.MessagesStorage$emptyMediaLambda")
)
```

---

## 4. Step-by-step update

### Step 1 — Audit the old JSON

```bash
python3 scripts/audit_obfuscation_mapping.py \
  app/src/main/assets/obfuscation_mappings/nekogram.json \
  /path/to/new/sources
```

Expect many stale entries after a version bump. Do not edit them one by one in
the JSON; fix them through overrides and regeneration.

### Step 2 — Identify removed or moved APIs

Search the JADX sources and the new mapping for the original names:

```bash
grep -rn "ApplicationLoaderImpl" /path/to/sources | head
grep -rn "processOnClickOrPress" /path/to/sources | head
grep -rn "emptyMessagesMedia" /path/to/mapping.txt | head
```

If the method is absent from both the source and the mapping, it was removed or
inlined. Find the caller or the synthetic lambda that contains the inlined body.

### Step 3 — Build overrides

For each unresolved member, add an override. Examples from the Nekogram 13.2.2
update are listed in section 5.

### Step 4 — Generate

Run the generator with `--report`. Check the summary:

```text
references: 72 classes; mapping: 23477 classes; warnings: 31
```

Open the report:

```bash
cat /tmp/nekogram-report.json
```

Look for:

- `METHOD NOT RENAMED`
- `FIELD NOT RENAMED`
- ambiguous overloads
- empty candidate lists

Each one must be either fixed with an override or intentionally left as the
original name.

### Step 5 — Build and install

```bash
./gradlew :app:assembleDebug :app:assembleRelease
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/release/app-release.apk
```

Force-stop and relaunch the target app so LSPosed loads the new module.

### Step 6 — Test feature by feature

Enable one feature at a time and check logcat:

```bash
adb logcat -c
adb logcat -d | grep -E "Telegami|Hook callback|NoSuchMethod|NoSuchField|ClassCast"
```

Fix runtime mapping errors with overrides, regenerate, and repeat. LSPosed
often keeps the old dex after `adb install -r`; a force-stop/relaunch is usually
enough, otherwise reboot.

---

## 5. Nekogram 13.2.2 lessons

These are the concrete cases encountered during the 13.2.2 update.

### `ProfileActivity` changed shape

- `onlineTextView` is no longer `SimpleTextView[]`; it is a single
  `AnimatedClickableSmallTextView` (`fh8`) with `M(CharSequence, boolean)`.
- `processOnClickOrPress` was replaced by a synthetic
  `lambda$createView$13` resolved as `z3`.
- The username row is `E4`.
- `updateOnlineText` is `R4`.
- The profile row click was later handled through
  `ProfileActivity.V3(float, float, int, View)` for the native username menu.

Override example:

```json
"org.telegram.ui.ProfileActivity": {
  "methods": {
    "processOnClickOrPress": "z3",
    "updateOnlineText": "R4",
    "usernameRowClick": "V3"
  },
  "fields": {
    "usernameRow": "E4"
  }
}
```

### Removed update APIs

`ApplicationLoaderImpl.isCustomUpdate` and `showUpdateAppPopup` no longer exist.
Update hiding moved to:

- `UpdateLayout.updateAppUpdateViews` / `IUpdateLayout`
- Nekogram `UpdateHelper` (`dee`) methods `d`, `g`, `e`, `i`

`UpdateHelper.d` is inherited from `BaseRemoteHelper` (`je0`), so the hook must
search superclasses and check the instance type before suppressing it.

### `emptyMessagesMedia` was inlined

The 2-arg `MessagesStorage.emptyMessagesMedia(long, ArrayList)` no longer exists
as a standalone method. The work moved into the shared synthetic lambda
`MessagesStorage$$ExternalSyntheticLambda30` (`m77`), type `1`.

Alias and hook the lambda's `run()` for type `1` instead of trying to hook the
removed wrapper.

### `MediaController.saveFile` signature changed

13.2.2 has:

```text
d0(String, Context, int, String, String, Callback)
```

The old 7-arg overload is gone. Resolve 6-arg first, then fall back to 7-arg for
older clients.

### `LocaleController.getString(int)` was removed

Many resources are no longer in the APK, and `getString(int)` is inlined. For
Nekogram, use the fallback-aware `M0(id, fallback)` variant. When a resource ID
is 0, return a literal fallback instead of `LOC_ERR:null`.

### `ColoredImageSpan.setOverrideColor` was removed

The field `overrideColor` (`M`) still exists. Calling the old mapped method
(`setColorKey`) causes the icon to use the text paint color. Set the field
directly.

### `TL_peerColor` has no usable constructor

Use `Unsafe.allocateInstance` to create one, then set the `flags`, `color`, and
`background_emoji_id` fields from the base `PeerColor` class.

### Proxy sponsor logic moved

`lambda$checkPromoInfo` became `MessagesController.checkPromoInfoInternal`
(`L`). Skip that entry point and clear the promo prefs through
`getGlobalMainSettings` (`a1`). Do not call it during `Application.attach`; the
app context is not ready and `MessagesController.getInstance()` will NPE.

### Merged TL request classes

Several request classes are merged into shared obfuscated classes:

```text
stories.readStories             -> vfb type 12
messages.readHistory            -> vfb type 5
messages.readMessageContents    -> pab type 9
messages.readEncryptedHistory   -> uv6 type 2
channels.readHistory            -> oab type 2
channels.readMessageContents    -> sab type 1
premium boosts                  -> u9d, etc.
```

Always gate by the discriminator byte and use the correct response type:

- `affectedMessages` (`i0c`) for private read-history/read-contents
- `Bool` (`aza` / `TL_boolTrue`) for channel/encrypted reads

Returning `affectedMessages` for a request whose callback expects
`messages_Messages` causes:

```text
ClassCastException: i0c cannot be cast to ioc
```

### Fields on subclasses

Class hierarchies can hide fields. If a field is declared on a base class
(`l42`) but the runtime object is a subclass (`asd`), the lookup must search
superclasses. Add `superclass()` to the field condition.

---

## 6. Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `NoSuchMethodException` with class dump | method removed/inlined or wrong overload | find caller/lambda; add override with `superclass()` or new name |
| `NoSuchFieldException` on a subclass | field on base class | add `superclass()` to field condition |
| `ClassCastException` between TL types | merged request class, wrong response type | gate by discriminator; return the correct TL response |
| Hook installs but feature does nothing | wrong method/overload or wrong class | verify mapping, add hint/override, test |
| Old behavior after install | LSPosed cached old dex | force-stop/relaunch or reboot |
| JSON edits disappear | generator rewrites from existing + overrides | put changes in the overrides file, not the output JSON |
| `NoSuchFieldException` for a TL field on one client only | R8 renamed that `TLRPC` field in one build | add the field to that variant's JSON (e.g. Nekogram 13.2.2 `TLRPC$Message.edit_date -> v`, while official keeps `edit_date`) and make the lookup return `0` when absent |

---

## 7. Checklist

- [ ] New APK, `mapping.txt`, JADX sources available
- [ ] Old JSON audited
- [ ] Removed/inlined APIs identified
- [ ] Overrides added for ambiguous/merged/synthetic cases
- [ ] `--report` inspected; warnings understood
- [ ] Debug + release built and installed
- [ ] Target app force-stopped/relaunched
- [ ] Each feature tested against logcat
- [ ] Temporary diagnostic logs removed
- [ ] JSON committed only when the user asks
