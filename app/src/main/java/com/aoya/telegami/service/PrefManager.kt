package com.aoya.telegami.service

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.aoya.telegami.telegamiApp

object PrefManager {
    private val appPref = telegamiApp.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val featPref = telegamiApp.getSharedPreferences("features", Context.MODE_WORLD_READABLE)

    private const val PREF_SYSTEM_WALLPAPER = "system_wallpaper"
    private const val PREF_SYSTEM_WALLPAPER_ALPHA = "system_wallpaper_alpha"
    private const val PREF_DARK_THEME = "dark_theme"
    private const val PREF_BLACK_DARK_THEME = "black_dark_theme"
    private const val PREF_FOLLOW_SYSTEM_ACCENT = "follow_system_accent"
    private const val PREF_THEME_COLOR = "theme_color"

    fun setFeatureEnabled(
        context: Context,
        featureKey: String,
        enabled: Boolean,
    ) {
        featPref.edit().putBoolean(featureKey, enabled).apply()
    }

    fun setFeatureValue(
        context: Context,
        featureKey: String,
        value: Int,
    ) {
        featPref.edit().putInt(featureKey, value).apply()
    }

    fun isFeatureEnabled(
        context: Context,
        featureKey: String,
    ): Boolean = featPref.getBoolean(featureKey, false)

    fun getFeatureValue(
        context: Context,
        featureKey: String,
        defaultValue: Int = 0,
    ): Int = featPref.getInt(featureKey, defaultValue)

    fun setHideFromLauncher(
        context: Context,
        hide: Boolean,
    ) {
        appPref.edit().putBoolean("hide_from_launcher", hide).apply()
    }

    fun isHideFromLauncher(context: Context): Boolean = appPref.getBoolean("hide_from_launcher", false)

    fun getActiveVersion() = -1

    var darkTheme: Int
        get() = appPref.getInt(PREF_DARK_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) = appPref.edit { putInt(PREF_DARK_THEME, value) }

    var systemWallpaper: Boolean
        get() = appPref.getBoolean(PREF_SYSTEM_WALLPAPER, false)
        set(value) = appPref.edit { putBoolean(PREF_SYSTEM_WALLPAPER, value) }

    var systemWallpaperAlpha: Int
        get() = appPref.getInt(PREF_SYSTEM_WALLPAPER_ALPHA, 0xAA)
        set(value) = appPref.edit { putInt(PREF_SYSTEM_WALLPAPER_ALPHA, value) }

    var blackDarkTheme: Boolean
        get() = appPref.getBoolean(PREF_BLACK_DARK_THEME, false)
        set(value) = appPref.edit { putBoolean(PREF_BLACK_DARK_THEME, value) }

    var followSystemAccent: Boolean
        get() = appPref.getBoolean(PREF_FOLLOW_SYSTEM_ACCENT, true)
        set(value) = appPref.edit { putBoolean(PREF_FOLLOW_SYSTEM_ACCENT, value) }

    var themeColor: String
        get() = appPref.getString(PREF_THEME_COLOR, "MATERIAL_BLUE")!!
        set(value) = appPref.edit { putString(PREF_THEME_COLOR, value) }
}
