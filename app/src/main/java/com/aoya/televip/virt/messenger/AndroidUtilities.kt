package com.aoya.televip.virt.messenger

import com.aoya.televip.TeleVip
import com.aoya.televip.virt.tgnet.TLRPC
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class AndroidUtilities {
    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.AndroidUtilities"

        fun addToClipboard(text: String): Unit? =
            callStaticMethod(
                TeleVip.loadClass(resolver.get(OBJ_PATH)),
                resolver.getMethod(OBJ_PATH, "addToClipboard"),
                text,
            ) as? Unit
    }
}
