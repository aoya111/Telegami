package com.aoya.telegami.core

import android.content.Context

object AppConfig {
    fun setHideFromLauncher(
        context: Context,
        hide: Boolean,
    ) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("hide_from_launcher", hide).apply()
    }

    fun isHideFromLauncher(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("hide_from_launcher", false)
    }
}
