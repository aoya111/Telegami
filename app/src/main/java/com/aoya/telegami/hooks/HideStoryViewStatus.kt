package com.aoya.telegami.hooks

import com.aoya.telegami.core.Config
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object HideStoryViewStatus : YukiBaseHooker() {
    const val STORIES_CONTROLLER_CN = "org.telegram.ui.Stories.StoriesController"
    val storiesControllerClass by lazyClass(resolver.get(STORIES_CONTROLLER_CN))

    override fun onHook() {
        if (!Config.isFeatureEnabled("HideStoryViewStatus")) return

        storiesControllerClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(STORIES_CONTROLLER_CN, "markStoryAsRead")
            }.hook {
                replaceToFalse()
            }
    }
}
