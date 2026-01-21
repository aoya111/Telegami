package com.aoya.telegami.virt.ui.actionbar

import android.graphics.drawable.Drawable
import com.aoya.telegami.virt.ui.actionbar.ActionBarMenu
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.newInstance
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ActionBar(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.ActionBar.ActionBar"

    val menu: ActionBarMenu?
        get() = getObjectField(instance, resolver.getField(objPath, "menu"))?.let { ActionBarMenu(it) }

    fun createMenu(): ActionBarMenu = ActionBarMenu(callMethod(instance, resolver.getMethod(objPath, "createMenu")))
}
