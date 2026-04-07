package com.aoya.telegami.hooks

import com.aoya.telegami.util.Hook
import com.aoya.telegami.util.HookStage

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
