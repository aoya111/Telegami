package com.aoya.televip.virt.messenger

import com.aoya.televip.TeleVip
import com.aoya.televip.virt.tgnet.TLRPC
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.getStaticBooleanField
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class ChatObject {
    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.ChatObject"

        fun isPublic(chat: TLRPC.Chat): Boolean =
            callStaticMethod(
                TeleVip.loadClass(resolver.get(OBJ_PATH)),
                resolver.getMethod(OBJ_PATH, "isPublic"),
                chat.getNativeInstance(),
            ) as Boolean

        fun getPublicUsername(chat: TLRPC.Chat): String? =
            callStaticMethod(
                TeleVip.loadClass(resolver.get(OBJ_PATH)),
                resolver.getMethod(OBJ_PATH, "getPublicUsername"),
                chat.getNativeInstance(),
            ) as? String
    }
}
