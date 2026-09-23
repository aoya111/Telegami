package com.aoya.telegami.virt.ui.components

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class UItem(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    val id: Int
        get() = instance.asResolver().firstField { this.name = resolver.getField(objPath, "id") }.get<Int>()!!

    companion object {
        private const val OBJ_PATH = "org.telegram.ui.Components.UItem"
    }
}
