package com.aoya.telegami.virt.ui.cells

import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.messenger.MessageObject
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setIntField
import de.robv.android.xposed.XposedHelpers.setObjectField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ChatMessageCell(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.Cells.ChatMessageCell"

    var timeWidth: Int
        get() = getIntField(instance, "timeWidth")
        set(value) = setIntField(instance, "timeWidth", value)

    var timeTextWidth: Int
        get() = getIntField(instance, "timeTextWidth")
        set(value) = setIntField(instance, "timeTextWidth", value)

    var currentTimeString: CharSequence
        get() = getObjectField(instance, resolver.getField(objPath, "currentTimeString")) as CharSequence
        set(value) = setObjectField(instance, resolver.getField(objPath, "currentTimeString"), value)

    fun getMessageObject(): MessageObject = MessageObject(callMethod(instance, resolver.getMethod(objPath, "getMessageObject")))
}
