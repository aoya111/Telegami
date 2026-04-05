package com.aoya.telegami.service

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.aoya.telegami.core.Constants.COMPONENT_NAME_DEFAULT
import com.aoya.telegami.telegamiApp
import com.aoya.telegami.utils.PackageHelper.findEnabledAppComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking

object PrefManager {
    private const val PREF_DARK_THEME = "dark_theme"
    private const val PREF_BLACK_DARK_THEME = "black_dark_theme"
    private const val PREF_FOLLOW_SYSTEM_ACCENT = "follow_system_accent"
    private const val PREF_THEME_COLOR = "theme_color"

    private val appPref = telegamiApp.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val featPref = telegamiApp.getSharedPreferences("features", Context.MODE_WORLD_READABLE)

    val isLauncherIconInvisible = MutableSharedFlow<Boolean>(replay = 1)

    fun setFeatureEnabled(
        featureKey: String,
        enabled: Boolean,
    ) {
        featPref.edit { putBoolean(featureKey, enabled) }
    }

    fun setFeatureValue(
        featureKey: String,
        value: Int,
    ) {
        featPref.edit { putInt(featureKey, value) }
    }

    fun isFeatureEnabled(featureKey: String): Boolean = featPref.getBoolean(featureKey, false)

    fun getFeatureValue(
        featureKey: String,
        defaultValue: Int = 0,
    ): Int = featPref.getInt(featureKey, defaultValue)

    fun getActiveVersion() = -1

    var darkTheme: Int
        get() = appPref.getInt(PREF_DARK_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) = appPref.edit { putInt(PREF_DARK_THEME, value) }

    var blackDarkTheme: Boolean
        get() = appPref.getBoolean(PREF_BLACK_DARK_THEME, false)
        set(value) = appPref.edit { putBoolean(PREF_BLACK_DARK_THEME, value) }

    var followSystemAccent: Boolean
        get() = appPref.getBoolean(PREF_FOLLOW_SYSTEM_ACCENT, true)
        set(value) = appPref.edit { putBoolean(PREF_FOLLOW_SYSTEM_ACCENT, value) }

    var themeColor: String
        get() = appPref.getString(PREF_THEME_COLOR, "MATERIAL_BLUE")!!
        set(value) = appPref.edit { putString(PREF_THEME_COLOR, value) }

    var hideIcon: Boolean
        get() = runCatching { isLauncherIconInvisible.replayCache.first() }.getOrElse { false }
        set(value) {
            val enabled = findEnabledAppComponent(telegamiApp)
            if (value && enabled != null) {
                telegamiApp.packageManager.setComponentEnabledSetting(
                    enabled,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP,
                )
            } else if (!value && enabled == null) {
                telegamiApp.packageManager.setComponentEnabledSetting(
                    ComponentName(telegamiApp, COMPONENT_NAME_DEFAULT),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP,
                )
            }

            runBlocking { isLauncherIconInvisible.emit(value) }
        }
}
