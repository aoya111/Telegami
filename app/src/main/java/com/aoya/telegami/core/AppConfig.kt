package com.aoya.telegami.core

import android.content.Context

object AppConfig {
    private fun appPrefs(context: Context) = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private fun featPrefs(context: Context) = context.getSharedPreferences("features", Context.MODE_WORLD_READABLE)

    fun setFeatureEnabled(
        context: Context,
        featureKey: String,
        enabled: Boolean,
    ) {
        featPrefs(context).edit().putBoolean(featureKey, enabled).apply()
    }

    fun setFeatureValue(
        context: Context,
        featureKey: String,
        value: Int,
    ) {
        featPrefs(context).edit().putInt(featureKey, value).apply()
    }

    fun isFeatureEnabled(
        context: Context,
        featureKey: String,
    ): Boolean = featPrefs(context).getBoolean(featureKey, false)

    fun getFeatureValue(
        context: Context,
        featureKey: String,
        defaultValue: Int = 0,
    ): Int = featPrefs(context).getInt(featureKey, defaultValue)

    fun setHideFromLauncher(
        context: Context,
        hide: Boolean,
    ) {
        appPrefs(context).edit().putBoolean("hide_from_launcher", hide).apply()
    }

    fun isHideFromLauncher(context: Context): Boolean = appPrefs(context).getBoolean("hide_from_launcher", false)
}
