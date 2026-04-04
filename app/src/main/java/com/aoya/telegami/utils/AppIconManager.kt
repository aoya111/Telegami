package com.aoya.telegami.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.aoya.telegami.service.PrefManager

object AppIconManager {
    private const val TAG = "AppIconManager"
    private const val LAUNCHER_COMPONENT = "com.aoya.telegami.MainActivityLauncher"

    private fun getLauncherComponent(context: Context): ComponentName = ComponentName(context.packageName, LAUNCHER_COMPONENT)

    private fun isLauncherEnabled(context: Context): Boolean {
        val component = getLauncherComponent(context)
        val state = context.packageManager.getComponentEnabledSetting(component)
        Log.d(TAG, "isLauncherEnabled: state=$state (1=ENABLED, 2=DISABLED)")
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }

    fun isHidden(context: Context): Boolean {
        val hidden = !isLauncherEnabled(context)
        Log.d(TAG, "isHidden: $hidden")
        return hidden
    }

    fun setHidden(
        context: Context,
        hidden: Boolean,
    ) {
        val component = getLauncherComponent(context)

        try {
            if (hidden) {
                context.packageManager.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP,
                )
            } else {
                context.packageManager.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP,
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting component state", e)
        }
        PrefManager.setHideFromLauncher(context, hidden)
    }

    fun applyState(context: Context) {
        setHidden(context, PrefManager.isHideFromLauncher(context))
    }
}
