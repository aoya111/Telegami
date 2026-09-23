package com.aoya.telegami.virt.messenger

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.tgnet.TLRPC
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class MessageObject(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    private val fieldMessageOwner by lazy { resolver.getField(objPath, "messageOwner") }
    private val methodGetId by lazy { resolver.getMethod(objPath, "getId") }
    private val methodGetDialogId by lazy { resolver.getMethod(objPath, "getDialogId") }
    private val methodIsSecretMedia by lazy { resolver.getMethod(objPath, "isSecretMedia") }

    val messageOwner: TLRPC.Message
        get() = TLRPC.Message(instance.asResolver().firstField { this.name = fieldMessageOwner }.get()!!)

    fun getId(): Int = instance.asResolver().firstMethod { this.name = methodGetId; parameters() }.invoke()!! as Int

    fun getDialogId(): Long = instance.asResolver().firstMethod { this.name = methodGetDialogId; parameters() }.invoke()!! as Long

    fun isSecretMedia(): Boolean = instance.asResolver().firstMethod { this.name = methodIsSecretMedia; parameters() }.invoke()!! as Boolean

    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.MessageObject"

        fun getMedia(o: Any): Any =
            (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>).resolve().firstMethod { this.name = resolver.getMethod(OBJ_PATH, "getMedia"); parameters(*arrayOf(o?.javaClass ?: Any::class.java)) }.invoke(o)!!
    }
}
