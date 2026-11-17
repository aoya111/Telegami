package com.aoya.telegami.hooks

import android.util.Log
import com.aoya.telegami.service.Config
import com.aoya.telegami.util.findMethod
import com.aoya.telegami.virt.tgnet.RequestDelegate
import com.aoya.telegami.virt.tgnet.TLRPC
import io.github.libxposed.api.XposedInterface
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object HideStoryViewStatus {
    const val CONNECTIONS_MANAGER_CN = "org.telegram.tgnet.ConnectionsManager"
    const val READ_STORIES_CN = "org.telegram.messenger.stories.ReadStoriesRequest"
    const val INCREMENT_VIEWS_CN = "org.telegram.messenger.stories.IncrementStoryViewsRequest"

    fun install(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        if (!Config.isFeatureEnabled("HideStoryViewStatus")) return

        // 12.x wraps these in the stories.*Request classes. 13.x sends the
        // TL_stories requests directly, so accept both shapes.
        val readStoriesClass = runCatching { classLoader.loadClass(resolver.get(READ_STORIES_CN)) }.getOrNull()
        val incrementViewsClass = runCatching { classLoader.loadClass(resolver.get(INCREMENT_VIEWS_CN)) }.getOrNull()
        val readStoriesTl =
            runCatching {
                classLoader.loadClass("org.telegram.tgnet.tl.TL_stories\$TL_stories_readStories")
            }.getOrNull()
        val incrementViewsTl =
            runCatching {
                classLoader.loadClass("org.telegram.tgnet.tl.TL_stories\$TL_stories_incrementStoryViews")
            }.getOrNull()

        val sendRequestInternal = classLoader.findMethod(CONNECTIONS_MANAGER_CN, "sendRequestInternal")
        xposed.hook(sendRequestInternal).intercept { chain ->
            val args = chain.args.toMutableList()
            try {
                val request = args[0]
                if (request != null) {
                    val type = typeOf(request)
                    val isReadStories =
                        readStoriesTl?.isInstance(request) == true ||
                            (readStoriesClass?.isInstance(request) == true && type == 12)
                    val isIncrementViews =
                        incrementViewsTl?.isInstance(request) == true ||
                            (incrementViewsClass?.isInstance(request) == true && type == 10)

                    if (isReadStories) {
                        // The normal read path sends this with a null callback.
                        // Let it update local state but never reach the server.
                        if (args[1] == null) {
                            return@intercept null
                        }
                    } else if (isIncrementViews) {
                        // stories.incrementStoryViews returns Bool. Complete the
                        // caller's callback locally without sending the view.
                        val callback = args[1]
                        if (callback != null) {
                            val boolTrue = TLRPC.TLBoolTrue.allocate()
                            if (boolTrue != null) {
                                RequestDelegate(callback).run(boolTrue.getNativeInstance(), null)
                                return@intercept null
                            }
                        } else {
                            return@intercept null
                        }
                    }
                }
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed before ${sendRequestInternal.name}", throwable)
            }
            chain.proceed(args.toTypedArray())
        }
    }

    private fun typeOf(request: Any): Int? {
        var clazz: Class<*>? = request.javaClass
        while (clazz != null) {
            val field =
                clazz.declaredFields.firstOrNull {
                    it.name == "a" && it.type == Byte::class.javaPrimitiveType
                }
            if (field != null) {
                field.isAccessible = true
                return (field.get(request) as? Byte)?.toInt()
            }
            clazz = clazz.superclass
        }
        return null
    }
}
