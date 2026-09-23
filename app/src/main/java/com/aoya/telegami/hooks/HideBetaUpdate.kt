package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.util.findMethod
import io.github.libxposed.api.XposedInterface

object HideBetaUpdate {
    const val APPLICATION_LOADER_IMPL_CN = "org.telegram.messenger.ApplicationLoaderImpl"

    fun install(xposed: XposedInterface, classLoader: ClassLoader) {
        if (Telegami.packageName != "org.telegram.messenger.beta") return
        xposed.hook(classLoader.findMethod(APPLICATION_LOADER_IMPL_CN, "isCustomUpdate")).intercept { false }
    }
}
