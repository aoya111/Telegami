package com.aoya.telegami.virt.ui.actionbar

import android.graphics.drawable.Drawable
import de.robv.android.xposed.XposedHelpers.callMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ActionBarMenuItem(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.ActionBar.ActionBarMenuItem"

    fun lazilyAddColoredGap() = callMethod(instance, resolver.getMethod(objPath, "lazilyAddColoredGap"))

    fun lazilyAddSubItem(
        id: Int,
        iconDrawable: Drawable?,
        text: String,
    ) = callMethod(instance, resolver.getMethod(objPath, "lazilyAddSubItem"), id, iconDrawable, text)
}
