package com.aoya.telegami.virt.messenger

import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.tgnet.TLRPC
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.getStaticBooleanField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ChatObject {
    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.ChatObject"

        fun isPublic(chat: TLRPC.Chat): Boolean =
            callStaticMethod(
                Telegami.loadClass(resolver.get(OBJ_PATH)),
                resolver.getMethod(OBJ_PATH, "isPublic"),
                chat.getNativeInstance(),
            ) as Boolean

        fun getPublicUsername(chat: TLRPC.Chat): String? =
            callStaticMethod(
                Telegami.loadClass(resolver.get(OBJ_PATH)),
                resolver.getMethod(OBJ_PATH, "getPublicUsername"),
                chat.getNativeInstance(),
            ) as? String
    }
}
