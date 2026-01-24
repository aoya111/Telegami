package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.hook
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class HideStoryViewStatus :
    Hook(
        "hide_story_view_status",
        "Hide 'Story View' status",
    ) {
    override fun init() {
        findClass("org.telegram.ui.Stories.StoriesController")
            .hook(
                resolver.getMethod("org.telegram.ui.Stories.StoriesController", "markStoryAsRead"),
                HookStage.BEFORE,
            ) { param ->
                if (!isEnabled) return@hook
                param.setResult(false)
            }
    }
}
