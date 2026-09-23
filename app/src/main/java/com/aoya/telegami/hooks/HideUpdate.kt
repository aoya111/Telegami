package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.util.findMethod
import io.github.libxposed.api.XposedInterface

object HideUpdate {
    const val APPLICATION_LOADER_IMPL_CN = "org.telegram.messenger.ApplicationLoaderImpl"
    const val UPDATE_LAYOUT_CN = "org.telegram.ui.Components.UpdateLayout"

    fun install(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        when (Telegami.packageName) {
            "org.telegram.messenger.beta" -> {
                xposed.hook(classLoader.findMethod(APPLICATION_LOADER_IMPL_CN, "isCustomUpdate")).intercept { false }
            }

            "tw.nekomimi.nekogram" -> {
                xposed.hook(classLoader.findMethod(APPLICATION_LOADER_IMPL_CN, "showUpdateAppPopup", 3)).intercept { true }
                xposed.hook(classLoader.findMethod(UPDATE_LAYOUT_CN, "updateAppUpdateViews", 2)).intercept { null }
            }
        }
    }
}
