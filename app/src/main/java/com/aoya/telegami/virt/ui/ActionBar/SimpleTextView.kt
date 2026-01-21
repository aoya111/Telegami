package com.aoya.telegami.virt.ui.actionbar

import de.robv.android.xposed.XposedHelpers.callMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

open class SimpleTextView(
    protected val instance: Any,
) {
    protected open val objPath = "org.telegram.ui.ActionBar.SimpleTextView"

    fun setText(text: String): Boolean = callMethod(instance, resolver.getMethod(objPath, "setText"), text) as Boolean
}
