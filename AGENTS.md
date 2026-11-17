# Telegami Development Guide

Essential information for agents working on the Telegami Xposed module project.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires keystore configuration)
./gradlew assembleRelease

# Clean build
./gradlew clean

# Install debug build to connected device
./gradlew installDebug

# Bump version (patch/minor/major)
./gradlew bumpVersion -Ptype=patch  # default
./gradlew bumpVersion -Ptype=minor
./gradlew bumpVersion -Ptype=major
```

## Testing

```bash
# Run all unit tests
./gradlew test

# Run a specific test class
./gradlew test --tests "com.aoya.telegami.ExampleTest"

# Run tests with detailed output
./gradlew test --info
```

## Lint and Code Quality

```bash
# Run Android lint checks
./gradlew lint

# Run lint on debug build
./gradlew lintDebug
```

## Project Structure

- **Package**: `com.aoya.telegami`
- **Main Entry**: `Telegami.kt` - Application singleton
- **Hook System**: `utils/Hook.kt` - Base class for Xposed hooks
- **Configuration**: `core/Config.kt` - User preferences
- **Database**: `data/AppDatabase.kt` - Room database

### Key Directories
- `hooks/` - Xposed hook implementations
- `core/` - Core utilities, i18n, obfuscation handling
- `utils/` - Helper classes
- `virt/` - Virtual classes mirroring Telegram's internal structure
- `data/` - Database entities and DAOs

## Code Style Guidelines

**Configured in `gradle.properties`**: `kotlin.code.style=official`

### Kotlin Conventions
- **4-space indentation**, no tabs
- **camelCase** for variables and functions
- **PascalCase** for classes and objects
- **UPPER_SNAKE_CASE** for constants

### Import Organization
```kotlin
// Android framework imports first
import android.app.Activity
import android.content.Context

// Third-party libraries
import de.robv.android.xposed.XC_MethodHook
import com.google.gson.Gson

// Project imports (grouped by package)
import com.aoya.telegami.Telegami
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.core.Config
```

### Naming Conventions
- **Boolean** variables: `isEnabled`, `hasConfig`, `shouldProcess`
- **Hook classes**: descriptive feature names like `HideSeenStatus`, `ShowDeletedMessages`
- **Utility functions**: simple action names like `logd`, `loge`, `findClass`

### Error Handling
- Use **custom logger** from `utils/Logger.kt`
- **Log levels**: `v()`, `d()`, `i()`, `w()`, `e()`
- Extension functions `logd()`, `loge()` available on any object
- **Build configuration** controls logging: `BuildConfig.ENABLE_LOGS`
- Always log errors with context

```kotlin
try {
    // risky operation
} catch (e: Exception) {
    loge("Failed to initialize hook", e)
}
```

### Hook Implementation Pattern
```kotlin
class FeatureHook : Hook("hook_name", "Hook description") {
    override fun init() {
        findAndHook(
            className = "org.telegram.messenger.TargetClass",
            methodName = "targetMethod",
            stage = HookStage.BEFORE,
            filter = { isEnabled },
            consumer = { param ->
                param.setResult(null)
            }
        )
    }
}
```

### Configuration
- **Hook settings** stored in `Config` object
- Enable/disable: `Config.setHookEnabled(name, enabled)`
- Check state: `Config.isHookEnabled(name)`

### Database
- **Room database** via `Telegami.db`
- **DAOs** for database operations
- **Entities** for data models

### Class Loading
- Use `Telegami.loadClass()` for Telegram classes
- Obfuscated names resolved through `ResolverManager`
- Use `Hook.findClass()` for class finding

## Development Workflow

### Adding New Hooks
1. Create hook class extending `Hook` in `hooks/` package
2. Implement `init()` method with hook logic
3. Add hook to `HookManager` initialization
4. Add configuration option in settings if needed
5. Test with both debug and release builds

### Virtual Classes
- Mirror Telegram's internal structure in `virt/`
- Keep in sync with actual Telegram classes
- Update obfuscation mappings when Telegram updates

## Build Variants

- **Debug**: `applicationIdSuffix = ".debug"`, `versionNameSuffix = "-DEBUG"`, `ENABLE_LOGS = true`
- **Release**: `ENABLE_LOGS = false`, minification disabled (currently)

## Important Notes

- This is an **Xposed module** - hooks run in target process context
- **Obfuscation handling** is critical for compatibility
- **Multi-client support** - handle different Telegram variants
- **Privacy considerations** - handle user data responsibly