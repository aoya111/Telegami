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

    private val fieldCurrentTimeString by lazy { resolver.getField(objPath, "currentTimeString") }
    private val methodGetMessageObject by lazy { resolver.getMethod(objPath, "getMessageObject") }

    var timeWidth: Int
        get() = getIntField(instance, "timeWidth")
        set(value) = setIntField(instance, "timeWidth", value)

    var timeTextWidth: Int
        get() = getIntField(instance, "timeTextWidth")
        set(value) = setIntField(instance, "timeTextWidth", value)

    var backgroundWidth: Int
        get() = getIntField(instance, "backgroundWidth")
        set(value) = setIntField(instance, "backgroundWidth", value)

    var currentTimeString: CharSequence
        get() = getObjectField(instance, fieldCurrentTimeString) as CharSequence
        set(value) = setObjectField(instance, fieldCurrentTimeString, value)

    fun getMessageObject(): MessageObject = MessageObject(callMethod(instance, methodGetMessageObject))
}
