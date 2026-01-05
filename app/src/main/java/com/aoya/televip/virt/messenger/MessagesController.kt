package com.aoya.televip.virt.messenger

import com.aoya.televip.virt.tgnet.TLRPC
import de.robv.android.xposed.XposedHelpers.callMethod
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class MessagesController(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.MessagesController"

    fun getUser(id: Long): TLRPC.User? = callMethod(instance, resolver.getMethod(objPath, "getUser"), id)?.let { TLRPC.User(it) }

    fun getChat(id: Long): TLRPC.Chat? = callMethod(instance, resolver.getMethod(objPath, "getChat"), id)?.let { TLRPC.Chat(it) }
}
