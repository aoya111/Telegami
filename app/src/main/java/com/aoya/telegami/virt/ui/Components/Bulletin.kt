package com.aoya.telegami.virt.ui.components

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class Bulletin(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    fun show(): Any? =
        instance
            .asResolver()
            .firstMethod {
                name = resolver.getMethod(objPath, "show")
                parameterCount = 0
            }.invoke()!!

    companion object {
        private const val OBJ_PATH = "org.telegram.ui.Components.Bulletin"
    }
}
