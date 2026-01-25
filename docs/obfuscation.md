# Obfuscation Mappings JSON Guide

<!--toc:start-->

- [Obfuscation Mappings JSON Guide](#obfuscation-mappings-json-guide)
  - [1. JSON Location and Naming](#1-json-location-and-naming)
  - [2. JSON Structure Overview](#2-json-structure-overview)
  - [3. Example: Minimal New Variant](#3-example-minimal-new-variant)
  - [4. Creating a JSON File for a New Variant](#4-creating-a-json-file-for-a-new-variant)
  - [5. Updating an Existing JSON File](#5-updating-an-existing-json-file)
  - [6. Runtime Behavior Reference](#6-runtime-behavior-reference)
  <!--toc:end-->

This document explains how to create and update JSON mapping files for new obfuscated Telegram variants (e.g., forks like Nekogram) used by Telegami’s `JsonResolver`.

## 1. JSON Location and Naming

JSON files are loaded from the Xposed module APK assets:

```text
app/src/main/assets/obfuscation_mappings/
  nekogram.json
  telegram_x.json
  nagram.json
  ...
```

File name convention:

- One file per variant
- Lowercase, no spaces
- `<variant>.json` (e.g. `nekogram.json`)

The variant name is mapped from package name in `ResolverManager`:

```kotlin
private val packageToVariant = mapOf(
    "tw.nekomimi.nekogram" to "nekogram",
    "org.telegram.messenger.web" to "telegram_x",
    "com.nagram.messenger" to "nagram",
)
```

If you add a new JSON file, you must also register it here.

---

## 2. JSON Structure Overview

Each JSON file describes **one variant**:

```json
{
  "variant": "nekogram",
  "package_name": "tw.nekomimi.nekogram",
  "version": "10.14.5",
  "updated": "2026-01-25",
  "mappings": {
    "<className>": {
      "obfuscated": "<obfuscated class name>",
      "hint": "<regex or textual hint>",
      "methods": {
        "<originalMethodName>": {
          "obfuscated": "<obfuscated method name>",
          "hint": "<regex or textual hint>"
        }
      },
      "fields": {
        "<originalFieldName>": {
          "obfuscated": "<obfuscated field name>",
          "hint": "<regex or textual hint>"
        }
      }
    }
  }
}
```

Notes:

- `package_name` must match the actual app package.
- `version` and `updated` are for humans (optional but recommended).
- `hint` is optional; use it to document how to find the member in a decompiled APK (regex snippet, string constant, etc.).

---

## 3. Example: Minimal New Variant

Example file `myfork.json`:

```json
{
  "variant": "myfork",
  "package_name": "com.example.myfork",
  "version": "1.0.0",
  "updated": "2026-01-25",
  "mappings": {
    "org.telegram.messenger.MessagesController": {
      "obfuscated": "org.telegram.messenger.m0",
      "hint": "SELECT data FROM app_config",
      "methods": {
        "getUser": {
          "obfuscated": "Mb",
          "hint": "return (TLRPC.\\w*) this.\\w*.get(\\w*); (with if)"
        },
        "deleteMessages": {
          "obfuscated": "n9",
          "hint": "(arraylist, arrayList2,"
        }
      },
      "fields": {}
    },
    "org.telegram.messenger.MessagesStorage": {
      "obfuscated": "org.telegram.messenger.n0",
      "hint": "\"messages_holes\"",
      "methods": {
        "emptyMessagesMedia": {
          "obfuscated": "R4",
          "hint": null
        }
      },
      "fields": {
        "database": {
          "obfuscated": "b",
          "hint": "UPDATE chat_settings_v2"
        }
      }
    }
  }
}
```

Then register it in `ResolverManager`:

```kotlin
private val packageToVariant = mapOf(
    "com.example.myfork" to "myfork",
    // existing mappings...
)
```

---

## 4. Creating a JSON File for a New Variant

1. **Copy an existing file as a template**

```bash
cp app/src/main/assets/obfuscation_mappings/nekogram.json \
   app/src/main/assets/obfuscation_mappings/myfork.json
```

1. **Edit top-level metadata**

In `myfork.json`:

```json
{
  "variant": "myfork",
  "package_name": "com.example.myfork",
  "version": "x.y.z",
  "updated": "YYYY-MM-DD",
  ...
}
```

1. **Adjust class mappings**

For each class whose obfuscation differs in the new variant: - Update the `obfuscated` value - Update `hint` to match the new pattern in the decompiled code

Example:

```json
"org.telegram.messenger.MessagesController": {
  "obfuscated": "a1",          // new obfuscated name
  "hint": "SELECT data FROM app_config",
  "methods": { ... },
  "fields": { ... }
}
```

1. **Adjust method mappings**

Inside `"methods"`:

```json
"methods": {
  "getUser": {
    "obfuscated": "Mb",
    "hint": "return (TLRPC.\\w*) this.\\w*.get(\\w*); (with if)"
  }
}
```

    - `getUser` is the original method name.
    - `"Mb"` is the obfuscated method name in the target APK.

5. **Adjust field mappings**

Inside `"fields"`:

```json
"fields": {
  "database": {
    "obfuscated": "b",
    "hint": "UPDATE chat_settings_v2"
  }
}
```

1. **Validate JSON**
   - Ensure valid JSON (no trailing commas, correct quotes).
   - Optional: use any JSON validator / linter.
2. **Rebuild / reinstall the module**

Once the JSON is in `assets/obfuscation_mappings/` and the mapping is registered in `ResolverManager`, rebuild the LSPosed module and restart the target app.

---

## 5. Updating an Existing JSON File

When a new app version changes obfuscation:

1. **Open the existing JSON**

For example:

```text
app/src/main/assets/obfuscation_mappings/nekogram.json
```

1. **Update metadata**

Change `version` and `updated`:

```json
"version": "10.15.0",
"updated": "2026-02-01",
```

1. **Update class obfuscation**

If the class name changed:

```json
"org.telegram.messenger.MessagesController": {
  "obfuscated": "x1",  // previously m0
  "hint": "SELECT data FROM app_config",
  ...
}
```

1. **Update method obfuscation**

If only methods changed:

```json
"methods": {
  "getUser": {
    "obfuscated": "Yb",   // previously Mb
    "hint": "return (TLRPC.\\w*) this.\\w*.get(\\w*); (with if)"
  }
}
```

Keep `hint` in sync with what you use to find the method in the decompiled APK (regex, string literal, etc.). 5. **Add new methods or fields**

If the module starts using new methods/fields, append entries:

```json
"methods": {
  "newImportantMethod": {
    "obfuscated": "Z1",
    "hint": "some unique string or pattern"
  }
}
```

1. **Remove obsolete entries (optional)**

If certain mappings are no longer used, you can delete them to keep the file tidy. The resolver will fall back to the original name for anything missing. 7. **Test** - Rebuild and install the module. - Enable logs (e.g. via `XposedBridge.log`) to verify that `JsonResolver` reports loading the correct variant and class count. - If you see missing mappings at runtime, check the spelling of `className`, `methodName`, and `fieldName` keys: they must match what you pass into `ResolverManager.get*`.

---

## 6. Runtime Behavior Reference

- If a class/method/field is **present** in JSON, Telegami will use the `obfuscated` value.
- If it is **missing**, it will fall back to the original name (no crash, just no deobfuscation).
- If JSON is invalid or missing, `ResolverManager` falls back to the `Default` resolver (no deobfuscation for that variant).

This design makes it safe to incrementally update JSON files without breaking the module.
