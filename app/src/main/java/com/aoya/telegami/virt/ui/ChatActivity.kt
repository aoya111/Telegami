package com.aoya.telegami.virt.ui

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ChatActivity(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.ChatActivity"

    private val fieldDialogId by lazy { resolver.getField(objPath, "dialog_id") }
    private val fieldCanShowPagedownButton by lazy { resolver.getField(objPath, "canShowPagedownButton") }
    private val fieldPagedownButtonShowedByScroll by lazy { resolver.getField(objPath, "pagedownButtonShowedByScroll") }

    val dialogId: Long
        get() = instance.asResolver().firstField { this.name = fieldDialogId }.get<Long>()!!

    var canShowPagedownButton: Boolean
        get() = instance.asResolver().firstField { this.name = fieldCanShowPagedownButton }.get<Boolean>()!!
        set(value) = instance.asResolver().firstField { this.name = fieldCanShowPagedownButton }.set(value)

    var pagedownButtonShowedByScroll: Boolean
        get() = instance.asResolver().firstField { this.name = fieldPagedownButtonShowedByScroll }.get<Boolean>()!!
        set(value) = instance.asResolver().firstField { this.name = fieldPagedownButtonShowedByScroll }.set(value)
}
