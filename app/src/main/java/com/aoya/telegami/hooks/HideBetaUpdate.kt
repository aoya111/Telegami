package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object HideBetaUpdate : YukiBaseHooker() {
    const val APPLICATION_LOADER_IMPL_CN = "org.telegram.messenger.ApplicationLoaderImpl"

    val applicationLoaderImplClass by lazyClass(resolver.get(APPLICATION_LOADER_IMPL_CN))

    override fun onHook() {
        if (Telegami.packageName != "org.telegram.messenger.beta") return
        applicationLoaderImplClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(APPLICATION_LOADER_IMPL_CN, "isCustomUpdate")
            }.hook {
                replaceToFalse()
            }
    }
}
