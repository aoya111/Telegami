package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.util.findMethod
import com.aoya.telegami.util.logw
import io.github.libxposed.api.XposedInterface
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object HideUpdate {
    const val APPLICATION_LOADER_IMPL_CN = "org.telegram.messenger.ApplicationLoaderImpl"
    const val UPDATE_LAYOUT_CN = "org.telegram.ui.Components.UpdateLayout"
    const val NEKO_UPDATE_HELPER_CN = "tw.nekomimi.nekogram.helpers.remote.UpdateHelper"

    fun install(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        when (Telegami.packageName) {
            "org.telegram.messenger.beta" -> {
                runCatching {
                    xposed.hook(classLoader.findMethod(APPLICATION_LOADER_IMPL_CN, "isCustomUpdate")).intercept { false }
                }.onFailure {
                    logw("HideUpdate isCustomUpdate unavailable: ${it.message}")
                }
            }

            "tw.nekomimi.nekogram" -> {
                runCatching {
                    xposed.hook(classLoader.findMethod(UPDATE_LAYOUT_CN, "updateAppUpdateViews", 2)).intercept { null }
                }.onFailure {
                    logw("HideUpdate layout hook unavailable: ${it.message}")
                }

                // Nekogram 13.x moved the update check/popup to UpdateHelper.
                // d() is inherited from BaseRemoteHelper, so only suppress it
                // for UpdateHelper instances and let other helpers continue.
                val updateHelperClass = classLoader.loadClass(resolver.get(NEKO_UPDATE_HELPER_CN))
                listOf("d" to 1, "g" to 2, "e" to 2, "i" to 1).forEach { (name, count) ->
                    runCatching {
                        xposed.hook(classLoader.findMethod(NEKO_UPDATE_HELPER_CN, name, count)).intercept { chain ->
                            if (updateHelperClass.isInstance(chain.thisObject)) {
                                null
                            } else {
                                chain.proceed(chain.args.toTypedArray())
                            }
                        }
                    }.onFailure {
                        logw("HideUpdate UpdateHelper.$name unavailable: ${it.message}")
                    }
                }
            }
        }
    }
}
