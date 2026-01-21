package com.aoya.telegami.virt.ui

import android.app.Activity
import com.aoya.telegami.virt.messenger.ImageReceiver
import com.aoya.telegami.virt.messenger.MessageObject
import com.aoya.telegami.virt.ui.actionbar.ActionBar
import com.aoya.telegami.virt.ui.components.VideoPlayer
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getBooleanField
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.getObjectField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class SecretMediaViewer(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.SecretMediaViewer"

    val currentAccount: Int
        get() = getIntField(instance, resolver.getField(objPath, "currentAccount"))

    val parentActivity: Activity
        get() = getObjectField(instance, resolver.getField(objPath, "parentActivity")) as Activity

    val containerView: Any
        get() = getObjectField(instance, resolver.getField(objPath, "containerView"))

    val centerImage: ImageReceiver
        get() = ImageReceiver(getObjectField(instance, resolver.getField(objPath, "centerImage")))

    val videoPlayer: VideoPlayer
        get() = VideoPlayer(getObjectField(instance, resolver.getField(objPath, "videoPlayer")))

    val actionBar: ActionBar
        get() = ActionBar(getObjectField(instance, resolver.getField(objPath, "actionBar")))

    val secretDeleteTimer: Any
        get() = getObjectField(instance, resolver.getField(objPath, "secretDeleteTimer"))

    val currentMessageObject: MessageObject
        get() = MessageObject(getObjectField(instance, resolver.getField(objPath, "messageObject")))

    val isVideo: Boolean
        get() = getBooleanField(instance, resolver.getField(objPath, "isVideo"))
}
