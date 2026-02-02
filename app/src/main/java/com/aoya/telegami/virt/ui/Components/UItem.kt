package com.aoya.telegami.virt.ui.components

import de.robv.android.xposed.XposedHelpers.getIntField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class UItem(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    val id: Int
        get() = getIntField(instance, resolver.getField(objPath, "id"))

    companion object {
        private const val OBJ_PATH = "org.telegram.ui.Components.UItem"
    }
}
