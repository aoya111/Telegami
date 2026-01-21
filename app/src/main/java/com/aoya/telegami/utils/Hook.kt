package com.aoya.telegami.utils

import android.graphics.drawable.Drawable
import com.aoya.telegami.Telegami
import com.aoya.telegami.core.Config
import com.aoya.telegami.data.AppDatabase
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

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
    open fun cleanup() {
    }

    protected fun findClass(name: String): Class<*> = Telegami.loadClass(resolver.get(name))

    protected val isEnabled: Boolean
        get() {
            return Config.isHookEnabled(hookName)
        }

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

    protected val db: AppDatabase
        get() {
            return Telegami.db
        }

    protected fun getResource(
        name: String,
        type: String,
    ): Int =
        Telegami.context.resources.getIdentifier(
            name,
            type,
            Telegami.context.packageName,
        )

    protected fun getAttribute(name: String): Int = getResource(name, "attr")

    protected fun getStringResource(name: String): String = Telegami.context.getString(getResource(name, "string"))

    protected fun getDrawableResource(name: String): Drawable? =
        getResource(name, "drawable").takeIf { it != 0 }?.let {
            Telegami.context.getDrawable(it)
        }
}
