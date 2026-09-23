package com.aoya.telegami.virt.messenger

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import android.graphics.Bitmap
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ImageReceiver(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.ImageReceiver"

    fun getBitmap(): Bitmap =
        instance.asResolver().firstMethod { this.name = resolver.getMethod(objPath, "getBitmap"); parameters() }.invoke()!! as Bitmap
}
