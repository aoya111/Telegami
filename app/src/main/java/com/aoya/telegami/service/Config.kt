package com.aoya.telegami.service

import android.content.Context
import com.highcapable.yukihookapi.hook.factory.prefs
import com.highcapable.yukihookapi.hook.xposed.prefs.YukiHookPrefsBridge

object Config {
    private var featPref: YukiHookPrefsBridge? = null
    private val featureCache = mutableMapOf<String, Boolean>()

    fun init(context: Context) {
        loadFeatureCache(context)
    }

    private fun loadFeatureCache(context: Context) {
        featPref = context.prefs("features")
        featPref?.all()?.forEach { (key, value) ->
            if (value is Boolean) {
                featureCache[key] = value
            }
        }
    }

    fun isFeatureEnabled(featureKey: String): Boolean = featureCache[featureKey] ?: false

    fun getFeatureValue(
        featureKey: String,
        defaultValue: Int = 0,
    ): Int = featPref?.getInt(featureKey, defaultValue) ?: 0
}
