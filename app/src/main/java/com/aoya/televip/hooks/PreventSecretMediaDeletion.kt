package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setObjectField
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class PreventSecretMediaDeletion :
    Hook(
        "prevent_secret_media_deletion",
        "Prevent Deletion of Secret Media.",
    ) {
    override fun init() {
        findClass(
            "org.telegram.ui.ChatActivity",
        ).hook(
            resolver.getMethod("org.telegram.ui.ChatActivity", "sendSecretMessageRead"),
            HookStage.BEFORE,
        ) { param -> param.setResult(null) }

        findClass(
            "org.telegram.ui.ChatActivity",
        ).hook(
            resolver.getMethod("org.telegram.ui.ChatActivity", "sendSecretMediaDelete"),
            HookStage.BEFORE,
        ) { param -> param.setResult(null) }

        // findClass(
        //     "org.telegram.ui.SecretMediaViewer",
        // ).hook(resolver.getMethod("org.telegram.ui.SecretMediaViewer", "openMedia"), HookStage.BEFORE) { param ->
        //     param.setArg(1, null)
        //     param.setArg(2, null)
        //     val forwardingMessage = param.argNullable<Any>(0) ?: return@hook
        //     val msgOwner = getObjectField(forwardingMessage, "messageOwner") ?: return@hook
        //     setObjectField(msgOwner, resolver.getField("org.telegram.tgnet.TLRPC.Message", "ttl"), 0x7FFFFFFF)
        // }
        //
        // findClass(
        //     "org.telegram.ui.SecretMediaViewer",
        // ).hook(resolver.getMethod("org.telegram.ui.SecretMediaViewer", "closePhoto"), HookStage.BEFORE) { param ->
        //     setObjectField(param.thisObject(), "onClose", null)
        // }
    }
}
