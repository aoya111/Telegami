package com.aoya.telegami.virt.ui.actionbar

import android.text.TextPaint
import com.aoya.telegami.Telegami
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.getStaticObjectField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class Theme(
    private val instance: Any,
) {
    companion object {
        private const val OBJ_PATH = "org.telegram.ui.ActionBar.Theme"

        private val fieldChatTimePaint by lazy { resolver.getField(OBJ_PATH, "chat_timePaint") }
        private val methodGetActiveTheme by lazy { resolver.getMethod(OBJ_PATH, "getActiveTheme") }
        private val classTheme by lazy { Telegami.loadClass(resolver.get(OBJ_PATH)) }

        val chatTimePaint: TextPaint
            get() =
                getStaticObjectField(
                    classTheme,
                    fieldChatTimePaint,
                ) as TextPaint

        fun getActiveTheme(): ThemeInfo =
            ThemeInfo(
                callStaticMethod(
                    classTheme,
                    methodGetActiveTheme,
                ),
            )
    }

    class ThemeInfo(
        private val instance: Any,
    ) {
        companion object {
            private const val OBJ_PATH = "org.telegram.ui.ActionBar.Theme\$ThemeInfo"

            private val methodIsDark by lazy { resolver.getMethod(OBJ_PATH, "isDark") }
        }

        fun isDark(): Boolean = callMethod(instance, methodIsDark) as Boolean
    }
}
