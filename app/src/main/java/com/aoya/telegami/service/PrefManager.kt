package com.aoya.telegami.service

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatDelegate
import com.aoya.telegami.core.Constants.COMPONENT_NAME_DEFAULT
import com.aoya.telegami.telegamiApp
import com.aoya.telegami.util.PackageHelper.findEnabledAppComponent
import com.aoya.telegami.util.logi
import com.aoya.telegami.util.logw
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking

object PrefManager {
    private const val PREF_DARK_THEME = "dark_theme"
    private const val PREF_BLACK_DARK_THEME = "black_dark_theme"
    private const val PREF_FOLLOW_SYSTEM_ACCENT = "follow_system_accent"
    private const val PREF_THEME_COLOR = "theme_color"

    private val appPref by lazy { telegamiApp.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }

    @Volatile
    private var xposedService: XposedService? = null

    val isLauncherIconInvisible = MutableSharedFlow<Boolean>(replay = 1)

    fun init() {
        XposedServiceHelper.registerListener(
            object : XposedServiceHelper.OnServiceListener {
                override fun onServiceBind(service: XposedService) {
                    xposedService = service
                    logi("Connected to LibXposed remote preferences")
                }

                override fun onServiceDied(service: XposedService) {
                    if (xposedService === service) {
                        xposedService = null
                        logw("Disconnected from LibXposed remote preferences")
                    }
                }
            },
        )
    }

    fun setFeatureEnabled(
        featureKey: String,
        enabled: Boolean,
    ) {
        featurePreferences()?.edit()?.putBoolean(featureKey, enabled)?.apply()
    }

    fun setFeatureValue(
        featureKey: String,
        value: Int,
    ) {
        featurePreferences()?.edit()?.putInt(featureKey, value)?.apply()
    }

    fun isFeatureEnabled(featureKey: String): Boolean = featurePreferences()?.getBoolean(featureKey, false) ?: false

    fun getFeatureValue(
        featureKey: String,
        defaultValue: Int = 0,
    ): Int = featurePreferences()?.getInt(featureKey, defaultValue) ?: defaultValue

    private fun featurePreferences() =
        xposedService
            ?.takeIf { it.frameworkProperties and XposedService.PROP_CAP_REMOTE != 0L }
            ?.getRemotePreferences("features")
            ?: run {
                logw("LibXposed remote preferences are unavailable")
                null
            }

    var darkTheme: Int
        get() = appPref.getInt(PREF_DARK_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) = appPref.edit().putInt(PREF_DARK_THEME, value).apply()

    var blackDarkTheme: Boolean
        get() = appPref.getBoolean(PREF_BLACK_DARK_THEME, false)
        set(value) = appPref.edit().putBoolean(PREF_BLACK_DARK_THEME, value).apply()

    var followSystemAccent: Boolean
        get() = appPref.getBoolean(PREF_FOLLOW_SYSTEM_ACCENT, true)
        set(value) = appPref.edit().putBoolean(PREF_FOLLOW_SYSTEM_ACCENT, value).apply()

    var themeColor: String
        get() = appPref.getString(PREF_THEME_COLOR, "MATERIAL_BLUE")!!
        set(value) = appPref.edit().putString(PREF_THEME_COLOR, value).apply()

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
