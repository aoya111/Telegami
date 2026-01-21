package com.aoya.telegami.virt.ui

import android.content.Context
import com.aoya.telegami.virt.ui.adapters.DrawerLayoutAdapter
import de.robv.android.xposed.XposedHelpers.getObjectField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

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
