package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook

class HideStoryViewStatus :
    Hook(
        "hide_story_view_status",
        "Hide 'Story View' status",
    ) {
    override fun init() {
        findClass("org.telegram.ui.Stories.StoriesController")
            .hook("markStoryAsRead", HookStage.BEFORE) { param -> param.setResult(false) }
    }
}
