package com.aoya.telegami.service

import android.content.SharedPreferences
import com.aoya.telegami.util.logi

object Config {
    private var featPref: SharedPreferences? = null
    private val featureCache = mutableMapOf<String, Boolean>()

    fun init(preferences: SharedPreferences) {
        featPref = preferences
        featureCache.clear()
        featPref?.all?.forEach { (key, value) ->
            if (value is Boolean) {
                featureCache[key] = value
            }
        }
        logi("Loaded ${featureCache.size} remote feature flags")
    }

    fun isFeatureEnabled(featureKey: String): Boolean {
        val enabled = featureCache[featureKey] ?: false
        logi("Read remote feature $featureKey: $enabled")
        return enabled
    }

    fun getFeatureValue(
        featureKey: String,
        defaultValue: Int = 0,
    ): Int {
        val value = featPref?.getInt(featureKey, defaultValue) ?: defaultValue
        logi("Read remote feature value $featureKey: $value")
        return value
    }
}
