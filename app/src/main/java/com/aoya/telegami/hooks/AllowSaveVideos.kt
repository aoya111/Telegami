package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.hook
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

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
