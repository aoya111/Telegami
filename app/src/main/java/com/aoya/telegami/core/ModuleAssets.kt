package com.aoya.telegami.core

import android.content.res.AssetManager

object ModuleAssets {
    fun open(
        modulePath: String,
        path: String,
    ) = run {
        val constructor = AssetManager::class.java.getDeclaredConstructor().apply { isAccessible = true }
        val manager = constructor.newInstance()
        AssetManager::class.java.getMethod("addAssetPath", String::class.java).invoke(manager, modulePath)
        manager.open(path)
    }
}
