package com.aoya.telegami.virt.ui.actionbar

import android.graphics.drawable.Drawable
import de.robv.android.xposed.XposedHelpers.callMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ActionBarMenu(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.ActionBar.ActionBarMenu"

    fun addItem(
        id: Int,
        drawable: Drawable,
    ): Any = callMethod(instance, resolver.getMethod(objPath, "addItem"), id, drawable)

    fun getItem(id: Int): Any = callMethod(instance, resolver.getMethod(objPath, "getItem"), id)
}
