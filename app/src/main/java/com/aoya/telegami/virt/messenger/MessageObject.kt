package com.aoya.telegami.virt.messenger

import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.tgnet.TLRPC
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.getObjectField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class MessageObject(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    val messageOwner: TLRPC.Message
        get() = TLRPC.Message(getObjectField(instance, resolver.getField(objPath, "messageOwner")))

    fun getId(): Int = callMethod(instance, resolver.getMethod(objPath, "getId")) as Int

    fun getDialogId(): Long = callMethod(instance, resolver.getMethod(objPath, "getDialogId")) as Long

    fun isSecretMedia(): Boolean = callMethod(instance, resolver.getMethod(objPath, "isSecretMedia")) as Boolean

    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.MessageObject"

        fun getMedia(o: Any): Any =
            callStaticMethod(
                Telegami.loadClass(resolver.get(OBJ_PATH)),
                resolver.getMethod(OBJ_PATH, "getMedia"),
                o,
            )
    }
}
