package com.aoya.telegami.virt.ui.components

import de.robv.android.xposed.XposedHelpers.callMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class Bulletin(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    fun show(): Any? = callMethod(instance, "show")

    companion object {
        private const val OBJ_PATH = "org.telegram.ui.Components.Bulletin"
    }
}
