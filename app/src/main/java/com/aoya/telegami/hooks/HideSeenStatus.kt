package com.aoya.telegami.hooks

import com.aoya.telegami.core.Config
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.messenger.MessagesController.ReadTask

class HideSeenStatus : Hook("HideSeenStatus") {
    override fun init() {
        findAndHook("org.telegram.messenger.MessagesController", "completeReadTask", HookStage.BEFORE) { param ->
            val dialogId = ReadTask(param.arg<Any>(0)).dialogId
            val currentUserId = Config.getCurrentUser().id

            val shouldHide =
                when {
                    dialogId == currentUserId -> false
                    dialogId > 0 -> Config.isFeatureEnabled("HideSeenPrivateChat")
                    else -> Config.isFeatureEnabled("HideSeenChannel")
                }

            if (shouldHide) {
                param.setResult(null)
            }
        }
    }
}
