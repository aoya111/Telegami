package com.aoya.telegami.virt.ui.cells

import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.messenger.MessageObject
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ChatMessageCell(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.Cells.ChatMessageCell"

    private val fieldCurrentTimeString by lazy { resolver.getField(objPath, "currentTimeString") }
    private val methodGetMessageObject by lazy { resolver.getMethod(objPath, "getMessageObject") }

    var timeWidth: Int
        get() = instance.asResolver().firstField { name = resolver.getField(objPath, "timeWidth"); superclass() }.get<Int>()!!
        set(value) = instance.asResolver().firstField { name = resolver.getField(objPath, "timeWidth"); superclass() }.set(value)

    var timeTextWidth: Int
        get() = instance.asResolver().firstField { name = resolver.getField(objPath, "timeTextWidth"); superclass() }.get<Int>()!!
        set(value) = instance.asResolver().firstField { name = resolver.getField(objPath, "timeTextWidth"); superclass() }.set(value)

    var backgroundWidth: Int
        get() = instance.asResolver().firstField { name = "backgroundWidth"; superclass() }.get<Int>()!!
        set(value) = instance.asResolver().firstField { name = "backgroundWidth"; superclass() }.set(value)

    var currentTimeString: CharSequence
        get() = instance.asResolver().firstField { this.name = fieldCurrentTimeString; superclass() }.get()!! as CharSequence
        set(value) = instance.asResolver().firstField { this.name = fieldCurrentTimeString; superclass() }.set(value)

    fun getMessageObject(): MessageObject =
        MessageObject(
            instance
                .asResolver()
                .firstMethod {
                    this.name = methodGetMessageObject
                    parameters()
                }.invoke()!!,
        )
}
