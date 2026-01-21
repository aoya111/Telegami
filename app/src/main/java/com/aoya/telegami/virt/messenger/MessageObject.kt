package com.aoya.telegami.virt.messenger

import de.robv.android.xposed.XposedHelpers.callMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class MessageObject(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.MessageObject"

    fun getId(): Int = callMethod(instance, resolver.getMethod(objPath, "getId")) as Int

    fun getDialogId(): Long = callMethod(instance, resolver.getMethod(objPath, "getDialogId")) as Long
}
