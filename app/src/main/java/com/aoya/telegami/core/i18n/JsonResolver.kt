package com.aoya.telegami.core.i18n

import android.content.res.XModuleResources
import com.aoya.telegami.utils.logd
import kotlinx.serialization.json.Json

class JsonResolver(
    private val mappings: Map<String, String>,
    private val fallbackMappings: Map<String, String>,
) : Translation {
    override fun get(key: String): String = mappings[key] ?: fallbackMappings[key] ?: ""

    companion object {
        private val json =
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }

        private fun loadTranslations(
            moduleRes: XModuleResources,
            localeCode: String,
        ): Map<String, String> =
            try {
                val jsonString =
                    moduleRes.assets
                        .open("translations/${localeCode.lowercase()}.json")
                        .bufferedReader()
                        .use { it.readText() }
                json.decodeFromString<Map<String, String>>(jsonString)
            } catch (e: Exception) {
                emptyMap()
            }

        fun fromModuleAssets(
            modulePath: String,
            localeCode: String,
        ): JsonResolver {
            val moduleRes = XModuleResources.createInstance(modulePath, null)

            val fallbackMappings = loadTranslations(moduleRes, "en")

            val mappings =
                if (localeCode.equals("en", ignoreCase = true)) {
                    fallbackMappings
                } else {
                    val langMappings = loadTranslations(moduleRes, localeCode)
                    fallbackMappings + langMappings
                }

            logd("Loaded $localeCode (${mappings.size}, fallback: ${fallbackMappings.size})")
            return JsonResolver(mappings, fallbackMappings)
        }
    }
}
