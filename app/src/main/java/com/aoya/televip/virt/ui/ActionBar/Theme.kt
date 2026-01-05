package com.aoya.televip.virt.ui.actionbar

import android.text.TextPaint
import com.aoya.televip.TeleVip
import de.robv.android.xposed.XposedHelpers.getStaticObjectField
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

open class Theme(
    protected val instance: Any,
) {
    companion object {
        private const val OBJ_PATH = "org.telegram.ui.ActionBar.Theme"

        val chatTimePaint: TextPaint
            get() =
                getStaticObjectField(
                    TeleVip.loadClass(resolver.get(OBJ_PATH)),
                    resolver.getField(OBJ_PATH, "chat_timePaint"),
                ) as TextPaint
    }
}
