package com.aoya.telegami.virt.ui.actionbar

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

open class SimpleTextView(
    protected val instance: Any,
) {
    protected open val objPath = "org.telegram.ui.ActionBar.SimpleTextView"

    fun setText(text: String): Boolean =
        instance
            .asResolver()
            .firstMethod {
                name = resolver.getMethod(objPath, "setText")
                parameterCount = 1
                superclass()
            }.invoke(text)!! as Boolean
}
