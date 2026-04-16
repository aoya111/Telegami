package com.aoya.telegami.hooks

import com.aoya.telegami.core.Config
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object AllowSaveVideos : YukiBaseHooker() {
    const val STORY_ITEM_HOLDER_CN = "org.telegram.ui.Stories.PeerStoriesView\$StoryItemHolder"
    val storyItemHolderClass by lazyClass(resolver.get(STORY_ITEM_HOLDER_CN))

    override fun onHook() {
        if (!Config.isFeatureEnabled("AllowSaveVideos")) return

        storyItemHolderClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(STORY_ITEM_HOLDER_CN, "allowScreenshots")
            }.hook {
                replaceToTrue()
            }
    }
}
