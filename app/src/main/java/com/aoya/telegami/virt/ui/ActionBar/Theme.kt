package com.aoya.telegami.virt.ui.actionbar

import android.text.TextPaint
import com.aoya.telegami.Telegami
import de.robv.android.xposed.XposedHelpers.getStaticObjectField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

open class Theme(
    protected val instance: Any,
) {
    companion object {
        private const val OBJ_PATH = "org.telegram.ui.ActionBar.Theme"

        val chatTimePaint: TextPaint
            get() =
                getStaticObjectField(
                    Telegami.loadClass(resolver.get(OBJ_PATH)),
                    resolver.getField(OBJ_PATH, "chat_timePaint"),
                ) as TextPaint
    }
}
