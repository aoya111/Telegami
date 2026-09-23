package com.aoya.telegami.virt.ui.actionbar

import android.graphics.drawable.Drawable
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ActionBarMenu(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.ActionBar.ActionBarMenu"

    fun addItem(
        id: Int,
        drawable: Drawable,
    ): Any =
        instance
            .asResolver()
            .firstMethod {
                name = resolver.getMethod(objPath, "addItem")
                parameters(Int::class.javaPrimitiveType!!, Drawable::class.java)
            }.invoke(id, drawable)!!

    fun getItem(id: Int): Any? =
        instance
            .asResolver()
            .firstMethod {
                name = resolver.getMethod(objPath, "getItem")
                parameters(Int::class.javaPrimitiveType!!)
            }.invoke(id)
}
