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

        val chatTimePaint: TextPaint
            get() =
                getStaticObjectField(
                    Telegami.loadClass(resolver.get(OBJ_PATH)),
                    resolver.getField(OBJ_PATH, "chat_timePaint"),
                ) as TextPaint

        fun getActiveTheme(): ThemeInfo =
            ThemeInfo(
                callStaticMethod(
                    Telegami.loadClass(resolver.get(OBJ_PATH)),
                    resolver.getMethod(OBJ_PATH, "getActiveTheme"),
                ),
            )
    }

    class ThemeInfo(
        private val instance: Any,
    ) {
        private val objPath = OBJ_PATH

        fun isDark(): Boolean = callMethod(instance, resolver.getMethod(objPath, "isDark")) as Boolean

        companion object {
            private const val OBJ_PATH = "org.telegram.ui.ActionBar.Theme\$ThemeInfo"
        }
    }
}
