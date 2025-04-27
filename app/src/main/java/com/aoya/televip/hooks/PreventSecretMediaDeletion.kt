package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setObjectField

class PreventSecretMediaDeletion :
    Hook(
        "prevent_secret_media_deletion",
        "Prevent Deletion of Secret Media.",
    ) {
    override fun init() {
        findClass(
            "org.telegram.ui.ChatActivity",
        ).hook("sendSecretMessageRead", HookStage.BEFORE) { param -> param.setResult(null) }

        findClass(
            "org.telegram.ui.ChatActivity",
        ).hook("sendSecretMediaDelete", HookStage.BEFORE) { param -> param.setResult(null) }

        findClass(
            "org.telegram.ui.SecretMediaViewer",
        ).hook("openMedia", HookStage.BEFORE) { param ->
            val forwardingMessage = param.argNullable<Any>(0) ?: return@hook
            val msgOwner = getObjectField(forwardingMessage, "messageOwner") ?: return@hook
            setObjectField(msgOwner, "ttl", 0x7FFFFFFF)
        }

        findClass(
            "org.telegram.ui.SecretMediaViewer",
        ).hook("closePhoto", HookStage.BEFORE) { param ->
            setObjectField(param.thisObject(), "onClose", null)
        }
    }
}
