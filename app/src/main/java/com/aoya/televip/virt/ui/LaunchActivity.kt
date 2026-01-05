package com.aoya.televip.virt.ui

import android.content.Context
import com.aoya.televip.virt.ui.adapters.DrawerLayoutAdapter
import de.robv.android.xposed.XposedHelpers.getObjectField
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class LaunchActivity(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.LaunchActivity"

    val context: Context
        get() = instance as Context

    val drawerLayoutAdapter: DrawerLayoutAdapter?
        get() = getObjectField(instance, "drawerLayoutAdapter")?.let { DrawerLayoutAdapter(it) }

    val drawerLayoutContainer: Any?
        get() = getObjectField(instance, "drawerLayoutContainer")
}
