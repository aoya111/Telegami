package com.aoya.telegami.hooks

import android.util.Log
import com.aoya.telegami.service.Config
import com.aoya.telegami.util.findMethod
import io.github.libxposed.api.XposedInterface

object AllowSaveVideos {
    const val STORY_ITEM_HOLDER_CN = "org.telegram.ui.Stories.PeerStoriesView\$StoryItemHolder"

    fun install(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        if (!Config.isFeatureEnabled("AllowSaveVideos")) return
        val method = classLoader.findMethod(STORY_ITEM_HOLDER_CN, "allowScreenshots")
        xposed.hook(method).intercept { chain ->
            val args = chain.args.toMutableList()
            var result: Any? = null
            var hasResult = false
            try {
                result = true
                hasResult = true
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed before ${method.name}", throwable)
            }
            if (!hasResult) {
                result = chain.proceed(args.toTypedArray())
                hasResult = true
            }
            result
        }
    }
}
