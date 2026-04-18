package com.aoya.telegami.virt.messenger

import com.aoya.telegami.virt.tgnet.TLRPC
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.extension.JLong
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class MessagesController(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.MessagesController"

    private val getUserMethod by lazy {
        instance
            .asResolver()
            .firstMethod {
                name = resolver.getMethod(objPath, "getUser")
                parameters(JLong::class)
            }
    }
    private val getChatMethod by lazy {
        instance
            .asResolver()
            .firstMethod {
                name = resolver.getMethod(objPath, "getChat")
                parameters(JLong::class)
            }
    }

    fun getUser(id: Long) =
        getUserMethod
            .copy()
            .of(instance)
            .invoke(id)
            ?.let { TLRPC.User(it) }

    fun getChat(id: Long) =
        getChatMethod
            .copy()
            .of(instance)
            .invoke(id)
            ?.let { TLRPC.Chat(it) }

    class ReadTask(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.messenger.MessagesController\$ReadTask"

        private val dialogIdField by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "dialogId")
                    type = Long::class
                }
        }

        val dialogId: Long
            get() = dialogIdField.get<Long>() ?: 0L
    }
}
