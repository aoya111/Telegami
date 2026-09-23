package com.aoya.telegami.virt.ui.components

import android.graphics.drawable.Drawable
import com.aoya.telegami.Telegami
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ColoredImageSpan(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    fun setSize(size: Int) {
        instance
            .asResolver()
            .firstMethod {
                name = resolver.getMethod(objPath, "setSize")
                parameters(Int::class.javaPrimitiveType!!)
            }.invoke(size)
    }

    fun setOverrideColor(color: Int) {
        instance
            .asResolver()
            .firstMethod {
                name = resolver.getMethod(objPath, "setOverrideColor")
                parameters(Int::class.javaPrimitiveType!!)
            }.invoke(color)
    }

    fun getNativeInstance() = instance

    companion object {
        private const val OBJ_PATH = "org.telegram.ui.Components.ColoredImageSpan"

        fun newInstance(drawable: Drawable): ColoredImageSpan =
            ColoredImageSpan(
                (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>)
                    .resolve()
                    .firstConstructor { parameters(Drawable::class.java) }
                    .create(drawable),
            )
    }
}
