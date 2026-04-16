package com.aoya.telegami.virt.messenger

import com.aoya.telegami.virt.tgnet.TLRPC
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class MessagesController(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.MessagesController"

    fun getUser(id: Long) =
        instance
            .asResolver()
            .firstMethod {
                name = resolver.getMethod(objPath, "getUser")
                parameters(Long::class)
            }.of(instance)
            .invoke(id)
            ?.let { TLRPC.User(it) }

    fun getChat(id: Long) =
        instance
            .asResolver()
            .firstMethod {
                name = resolver.getMethod(objPath, "getChat")
                parameters(Long::class)
            }.of(instance)
            .invoke(id)
            ?.let { TLRPC.Chat(it) }

    class ReadTask(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.messenger.MessagesController\$ReadTask"

        val dialogId
            get() =
                instance
                    .asResolver()
                    .firstField {
                        name = resolver.getField(objPath, "dialogId")
                        type = Long::class
                    }.get<Long>() ?: 0L
    }
}
