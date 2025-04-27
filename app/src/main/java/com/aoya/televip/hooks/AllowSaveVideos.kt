package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook

class AllowSaveVideos :
    Hook(
        "allow_save_videos",
        "Allow saving videos to the gallery",
    ) {
    override fun init() {
        findClass(
            "org.telegram.ui.Stories.PeerStoriesView\$StoryItemHolder",
        ).hook("allowScreenshots", HookStage.BEFORE) { param -> param.setResult(true) }
    }
}
