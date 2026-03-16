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

    private val fieldMessageOwner by lazy { resolver.getField(objPath, "messageOwner") }
    private val methodGetId by lazy { resolver.getMethod(objPath, "getId") }
    private val methodGetDialogId by lazy { resolver.getMethod(objPath, "getDialogId") }
    private val methodIsSecretMedia by lazy { resolver.getMethod(objPath, "isSecretMedia") }

    val messageOwner: TLRPC.Message
        get() = TLRPC.Message(getObjectField(instance, fieldMessageOwner))

    val id: Int
        get() = callMethod(instance, methodGetId) as Int

    val dialogId: Long
        get() = callMethod(instance, methodGetDialogId) as Long

    fun isSecretMedia(): Boolean = callMethod(instance, methodIsSecretMedia) as Boolean

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
