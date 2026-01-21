package com.aoya.telegami.virt.ui.components

import de.robv.android.xposed.XposedHelpers.callMethod
import java.io.File
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class VideoPlayer(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.Components.VideoPlayer"

    fun getFile(): File =
        callMethod(
            instance,
            resolver.getMethod(objPath, "getFile"),
        ) as File
}
