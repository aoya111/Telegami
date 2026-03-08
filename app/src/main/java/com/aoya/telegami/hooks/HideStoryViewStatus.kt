package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage

class HideStoryViewStatus : Hook("HideStoryViewStatus") {
    override fun init() {
        findAndHook(
            "org.telegram.ui.Stories.StoriesController",
            "markStoryAsRead",
            HookStage.BEFORE,
        ) { param ->
            param.setResult(false)
        }
    }
}
