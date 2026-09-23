package com.aoya.telegami.virt.messenger

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.tgnet.TLRPC
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ChatObject {
    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.ChatObject"

        fun isPublic(chat: TLRPC.Chat): Boolean =
            (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>).resolve().firstMethod { this.name = resolver.getMethod(OBJ_PATH, "isPublic"); parameters(*arrayOf(chat.getNativeInstance()?.javaClass ?: Any::class.java)) }.invoke(chat.getNativeInstance())!! as Boolean

        fun getPublicUsername(chat: TLRPC.Chat): String? =
            (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>).resolve().firstMethod { this.name = resolver.getMethod(OBJ_PATH, "getPublicUsername"); parameters(*arrayOf(chat.getNativeInstance()?.javaClass ?: Any::class.java)) }.invoke(chat.getNativeInstance())!! as? String
    }
}
