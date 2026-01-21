package com.aoya.telegami.virt.messenger

import android.graphics.Bitmap
import de.robv.android.xposed.XposedHelpers.callMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ImageReceiver(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.ImageReceiver"

    fun getBitmap(): Bitmap =
        callMethod(
            instance,
            resolver.getMethod(objPath, "getBitmap"),
        ) as Bitmap
}
