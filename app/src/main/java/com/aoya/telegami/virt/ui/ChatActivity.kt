package com.aoya.telegami.virt.ui

import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getBooleanField
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.setBooleanField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ChatActivity(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.ChatActivity"

    val dialogId: Long
        get() = getLongField(instance, resolver.getField(objPath, "dialog_id"))

    fun scrollToMessageId(
        id: Int,
        fromMessageId: Int,
        select: Boolean,
        loadIndex: Int,
        forceScroll: Boolean,
        forcePinnedMessageId: Int,
    ) = callMethod(
        instance,
        resolver.getMethod(objPath, "scrollToMessageId"),
        id,
        fromMessageId,
        select,
        loadIndex,
        forceScroll,
        forcePinnedMessageId,
    )

    fun updatePagedownButtonVisibility(animated: Boolean) =
        callMethod(
            instance,
            resolver.getMethod(objPath, "updatePagedownButtonVisibility"),
            animated,
        )

    var canShowPagedownButton: Boolean
        get() = getBooleanField(instance, resolver.getField(objPath, "canShowPagedownButton"))
        set(value) = setBooleanField(instance, resolver.getField(objPath, "canShowPagedownButton"), value)

    var pagedownButtonShowedByScroll: Boolean
        get() = getBooleanField(instance, resolver.getField(objPath, "pagedownButtonShowedByScroll"))
        set(value) = setBooleanField(instance, resolver.getField(objPath, "pagedownButtonShowedByScroll"), value)
}
