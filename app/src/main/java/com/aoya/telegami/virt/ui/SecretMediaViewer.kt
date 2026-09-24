package com.aoya.telegami.virt.ui

import android.app.Activity
import com.aoya.telegami.virt.messenger.ImageReceiver
import com.aoya.telegami.virt.messenger.MessageObject
import com.aoya.telegami.virt.ui.actionbar.ActionBar
import com.aoya.telegami.virt.ui.components.VideoPlayer
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class SecretMediaViewer(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.SecretMediaViewer"

    private val fieldCurrentAccount by lazy {
        instance
            .asResolver()
            .firstField {
                name = resolver.getField(objPath, "currentAccount")
            }
    }
    private val fieldParentActivity by lazy {
        instance.asResolver().firstField { name = resolver.getField(objPath, "parentActivity") }
    }
    private val fieldContainerView by lazy {
        instance.asResolver().firstField { name = resolver.getField(objPath, "containerView") }
    }
    private val fieldCenterImage by lazy {
        instance.asResolver().firstField { name = resolver.getField(objPath, "centerImage") }
    }
    private val fieldActionBar by lazy {
        instance.asResolver().firstField { name = resolver.getField(objPath, "actionBar") }
    }
    private val fieldSecretDeleteTimer by lazy {
        instance.asResolver().firstField { name = resolver.getField(objPath, "secretDeleteTimer") }
    }
    private val fieldCurrentMessageObject by lazy {
        instance.asResolver().firstField { name = resolver.getField(objPath, "messageObject") }
    }
    private val fieldIsVideo by lazy {
        instance.asResolver().firstField { name = resolver.getField(objPath, "isVideo") }
    }

    val currentAccount: Int
        get() = fieldCurrentAccount.get<Int>()!!

    val parentActivity: Activity
        get() = fieldParentActivity.get()!! as Activity

    val containerView: Any
        get() = fieldContainerView.get()!!

    val centerImage: ImageReceiver
        get() = ImageReceiver(fieldCenterImage.get()!!)

    val actionBar: ActionBar
        get() = ActionBar(fieldActionBar.get()!!)

    val secretDeleteTimer: Any
        get() = fieldSecretDeleteTimer.get()!!

    val currentMessageObject: MessageObject?
        get() = fieldCurrentMessageObject.get()?.let(::MessageObject)

    val isVideo: Boolean
        get() = fieldIsVideo.get<Boolean>()!!
}
