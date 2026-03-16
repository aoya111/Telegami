package com.aoya.telegami.virt.messenger

import com.aoya.telegami.virt.tgnet.TLRPC
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getLongField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class MessagesController(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.MessagesController"

    fun getUser(id: Long): TLRPC.User? = callMethod(instance, resolver.getMethod(objPath, "getUser"), id)?.let { TLRPC.User(it) }

    fun getChat(id: Long): TLRPC.Chat? = callMethod(instance, resolver.getMethod(objPath, "getChat"), id)?.let { TLRPC.Chat(it) }

    class ReadTask(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.messenger.MessagesController\$ReadTask"

        val dialogId: Long
            get() = getLongField(instance, resolver.getField(objPath, "dialogId"))
    }
}
