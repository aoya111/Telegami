package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage

class HideStoryViewStatus :
    Hook(
        "hide_story_view_status",
        "Hide 'Story View' status",
    ) {
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
