package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage

class AllowSaveVideos :
    Hook(
        "allow_save_videos",
        "Allow saving videos to the gallery",
    ) {
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
