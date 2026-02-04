package com.aoya.telegami.utils

import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.aoya.telegami.Telegami
import com.aoya.telegami.core.Config
import com.aoya.telegami.data.AppDatabase
import com.aoya.telegami.virt.ui.actionbar.Theme
import de.robv.android.xposed.XC_MethodHook
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

    protected fun findClass(name: String): Class<Any> = Telegami.loadClass(resolver.get(name)) as Class<Any>

    protected fun findAndHook(
        className: String,
        methodName: String,
        stage: HookStage,
        filter: (HookAdapter<Any>) -> Boolean = { isEnabled },
        consumer: (HookAdapter<Any>) -> Unit,
    ): Set<XC_MethodHook.Unhook> {
        val clazz = findClass(className)
        val obfuscatedMethod = resolver.getMethod(className, methodName)
        return clazz.hook(obfuscatedMethod, stage, filter, consumer)
    }

    protected val isEnabled: Boolean
        get() = Config.isHookEnabled(hookName)

    protected val isDark: Boolean
        get() = Theme.getActiveTheme().isDark()

    protected val db: AppDatabase
        get() = Telegami.db

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

    protected fun getStringResource(
        name: String,
        default: String = "",
    ): String {
        val resId = getResource(name, "string")
        if (resId == 0) return default

        return try {
            Telegami.context.getString(resId)
        } catch (e: Resources.NotFoundException) {
            default
        }
    }

    protected fun getDrawableResource(name: String): Drawable? =
        getResource(name, "drawable").takeIf { it != 0 }?.let {
            Telegami.context.getDrawable(it)
        }
}
