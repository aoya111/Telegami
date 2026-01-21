package com.aoya.telegami.virt.messenger

import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.tgnet.TLRPC
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class AndroidUtilities {
    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.AndroidUtilities"

        fun addToClipboard(text: String): Unit? =
            callStaticMethod(
                Telegami.loadClass(resolver.get(OBJ_PATH)),
                resolver.getMethod(OBJ_PATH, "addToClipboard"),
                text,
            ) as? Unit
    }
}
