package com.aoya.telegami.virt.ui

import de.robv.android.xposed.XposedHelpers.getBooleanField
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.setBooleanField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ChatActivity(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.ChatActivity"

    private val fieldDialogId by lazy { resolver.getField(objPath, "dialog_id") }
    private val fieldCanShowPagedownButton by lazy { resolver.getField(objPath, "canShowPagedownButton") }
    private val fieldPagedownButtonShowedByScroll by lazy { resolver.getField(objPath, "pagedownButtonShowedByScroll") }

    val dialogId: Long
        get() = getLongField(instance, fieldDialogId)

    var canShowPagedownButton: Boolean
        get() = getBooleanField(instance, fieldCanShowPagedownButton)
        set(value) = setBooleanField(instance, fieldCanShowPagedownButton, value)

    var pagedownButtonShowedByScroll: Boolean
        get() = getBooleanField(instance, fieldPagedownButtonShowedByScroll)
        set(value) = setBooleanField(instance, fieldPagedownButtonShowedByScroll, value)
}
