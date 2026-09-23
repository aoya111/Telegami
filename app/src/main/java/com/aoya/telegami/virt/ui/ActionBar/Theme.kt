package com.aoya.telegami.virt.ui.actionbar

import android.text.TextPaint
import com.aoya.telegami.Telegami
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class Theme(
    private val instance: Any,
) {
    companion object {
        private const val OBJ_PATH = "org.telegram.ui.ActionBar.Theme"

        private val fieldChatTimePaint by lazy { resolver.getField(OBJ_PATH, "chat_timePaint") }
        private val fieldActiveTheme by lazy { resolver.getField(OBJ_PATH, "activeTheme") }
        private val methodGetActiveTheme by lazy { resolver.getMethod(OBJ_PATH, "getActiveTheme") }
        private val classTheme by lazy { Telegami.loadClass(resolver.get(OBJ_PATH)) }

        val chatTimePaint: TextPaint
            get() =
                (classTheme as Class<Any>).resolve().firstField { this.name = fieldChatTimePaint }.get()!! as TextPaint

        fun getActiveTheme(): ThemeInfo {
            val theme =
                if (Telegami.packageName == "tw.nekomimi.nekogram") {
                    (classTheme as Class<Any>).resolve().firstField { name = fieldActiveTheme }.get()!!
                } else {
                    (classTheme as Class<Any>)
                        .resolve()
                        .firstMethod {
                            name = methodGetActiveTheme
                            parameters()
                        }.invoke()!!
                }
            return ThemeInfo(theme)
        }
    }

    class ThemeInfo(
        private val instance: Any,
    ) {
        companion object {
            private const val OBJ_PATH = "org.telegram.ui.ActionBar.Theme\$ThemeInfo"

            private val methodIsDark by lazy { resolver.getMethod(OBJ_PATH, "isDark") }
        }

        fun isDark(): Boolean =
            instance
                .asResolver()
                .firstMethod {
                    this.name = methodIsDark
                    parameters()
                }.invoke()!! as Boolean
    }
}
