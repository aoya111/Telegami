package com.aoya.telegami.virt.ui.cells

import android.widget.TextView
import de.robv.android.xposed.XposedHelpers.getObjectField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class DrawerProfileCell(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.Cells.DrawerProfileCell"

    val phoneTextView: TextView
        get() = getObjectField(instance, resolver.getField(objPath, "phoneTextView")) as TextView
}
