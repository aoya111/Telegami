package com.aoya.telegami.virt.messenger

import com.aoya.telegami.core.obfuscate.ResolverManager as resolver
import com.aoya.telegami.virt.tgnet.TLRPC

class MessagesController(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.MessagesController"
    private val getUserMethod by lazy { findMethod(resolver.getMethod(objPath, "getUser")) }
    private val getChatMethod by lazy { findMethod(resolver.getMethod(objPath, "getChat")) }

    fun getUser(id: Long) = getUserMethod.invoke(instance, id)?.let { TLRPC.User(it) }
    fun getChat(id: Long) = getChatMethod.invoke(instance, id)?.let { TLRPC.Chat(it) }

    class ReadTask(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.messenger.MessagesController\$ReadTask"
        private val dialogIdField by lazy {
            instance.javaClass.getDeclaredField(resolver.getField(objPath, "dialogId")).apply { isAccessible = true }
        }

        val dialogId: Long
            get() = (dialogIdField.get(instance) as? Long) ?: 0L
    }

    private fun findMethod(name: String) =
        instance.javaClass.methods.first { it.name == name && it.parameterCount == 1 }.apply { isAccessible = true }
}
