package com.aoya.telegami.hooks

import com.aoya.telegami.util.Hook
import com.aoya.telegami.util.HookStage

class AllowSaveVideos : Hook("AllowSaveVideos") {
    override fun init() {
        findAndHook(
            "org.telegram.ui.Stories.PeerStoriesView\$StoryItemHolder",
            "allowScreenshots",
            HookStage.BEFORE,
        ) { param ->
            param.setResult(true)
        }
    }
}
