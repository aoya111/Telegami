package com.aoya.televip.utils

import com.aoya.televip.TeleVip
import com.aoya.televip.core.Config
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

abstract class Hook(
    val hookName: String,
    val hookDesc: String = "",
) {
    /**
     * Hook specific initialization.
     */
    open fun init() {}

    /**
     * Hook specific cleanup.
     */
    open fun cleanup() {}

    protected fun isHookEnabled(): Boolean = Config.isHookEnabled(hookName)

    protected fun findClass(name: String): Class<*> = TeleVip.loadClass(resolver.get(name))

    protected val isDark: Boolean
        get() {
            try {
                val currentThemeInfo =
                    callStaticMethod(
                        findClass("org.telegram.ui.ActionBar.Theme"),
                        resolver.getMethod("org.telegram.ui.ActionBar.Theme", "getActiveTheme"),
                    )

                if (currentThemeInfo != null) {
                    return callMethod(
                        currentThemeInfo,
                        resolver.getMethod("org.telegram.ui.ActionBar.Theme\$ThemeInfo", "isDark"),
                    ) as Boolean
                }
            } catch (e: Exception) {
                return false
            }
            return false
        }

    protected fun getResource(
        name: String,
        type: String,
    ): Int =
        TeleVip.context.resources.getIdentifier(
            name,
            type,
            TeleVip.context.packageName,
        )

    protected fun getAttribute(name: String): Int = TeleVip.context.resources.getIdentifier(name, "attr", TeleVip.context.packageName)
}
