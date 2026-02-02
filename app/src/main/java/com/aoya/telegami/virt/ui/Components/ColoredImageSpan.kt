package com.aoya.telegami.virt.ui.components

import android.graphics.drawable.Drawable
import com.aoya.telegami.Telegami
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.newInstance
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ColoredImageSpan(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    fun setSize(size: Int) = callMethod(instance, resolver.getMethod(objPath, "setSize"), size) as Unit?

    fun getNativeInstance() = instance

    companion object {
        private const val OBJ_PATH = "org.telegram.ui.Components.ColoredImageSpan"

        fun newInstance(drawable: Drawable?): ColoredImageSpan =
            ColoredImageSpan(newInstance(Telegami.loadClass(resolver.get(OBJ_PATH)), drawable))
    }
}
