package com.aoya.telegami.virt.ui.actionbar

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import android.graphics.drawable.Drawable
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ActionBarMenuItem(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.ActionBar.ActionBarMenuItem"

    fun lazilyAddColoredGap() = instance.asResolver().firstMethod { this.name = resolver.getMethod(objPath, "lazilyAddColoredGap"); parameters() }.invoke()!!

    fun lazilyAddSubItem(
        id: Int,
        iconDrawable: Drawable?,
        text: String,
    ) = instance.asResolver().firstMethod { this.name = resolver.getMethod(objPath, "lazilyAddSubItem"); parameters(*arrayOf(id?.javaClass ?: Any::class.java, iconDrawable?.javaClass ?: Any::class.java, text?.javaClass ?: Any::class.java)) }.invoke(id, iconDrawable, text)!!
}
