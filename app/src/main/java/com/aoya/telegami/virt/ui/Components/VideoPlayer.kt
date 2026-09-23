package com.aoya.telegami.virt.ui.components

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import java.io.File
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class VideoPlayer(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.Components.VideoPlayer"

    fun getFile(): File =
        instance.asResolver().firstMethod { this.name = resolver.getMethod(objPath, "getFile"); parameters() }.invoke()!! as File
}
