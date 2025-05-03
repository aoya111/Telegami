package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class AllowSaveVideos :
    Hook(
        "allow_save_videos",
        "Allow saving videos to the gallery",
    ) {
    override fun init() {
        findClass(
            "org.telegram.ui.Stories.PeerStoriesView\$StoryItemHolder",
        ).hook(
            resolver.getMethod("org.telegram.ui.Stories.PeerStoriesView\$StoryItemHolder", "allowScreenshots"),
            HookStage.BEFORE,
        ) { param -> param.setResult(true) }
    }
}
