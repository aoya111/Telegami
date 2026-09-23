package com.aoya.telegami.core.i18n

import com.aoya.telegami.core.ModuleAssets
import com.aoya.telegami.util.logd
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
            modulePath: String,
            localeCode: String,
        ): Map<String, String> =
            try {
                val jsonString =
                    ModuleAssets.open(modulePath, "translations/${localeCode.lowercase()}.json")
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
            val fallbackMappings = loadTranslations(modulePath, "en")

            val mappings =
                if (localeCode.equals("en", ignoreCase = true)) {
                    fallbackMappings
                } else {
                    val langMappings = loadTranslations(modulePath, localeCode)
                    fallbackMappings + langMappings
                }

            logd("Loaded $localeCode (${mappings.size}, fallback: ${fallbackMappings.size})")
            return JsonResolver(mappings, fallbackMappings)
        }
    }
}
