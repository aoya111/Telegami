package com.aoya.telegami.core.i18n

import android.content.res.XModuleResources
import com.aoya.telegami.utils.logd
import kotlinx.serialization.json.Json

class JsonResolver(
    private val mappings: Map<String, String>,
) : Translation {
    override fun get(key: String): String = mappings[key] ?: ""

    companion object {
        private val json =
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }

        fun fromModuleAssets(
            modulePath: String,
            localeCode: String,
        ): JsonResolver {
            val moduleRes = XModuleResources.createInstance(modulePath, null)
            var jsonString = try {
                moduleRes.assets
                    .open("translations/${localeCode.lowercase()}.json")
                    .bufferedReader()
                    .use { it.readText() }
            } catch (e: Exception) {
                ""
            }
            
            if (jsonString.isEmpty()) {
                jsonString =
                    moduleRes.assets
                        .open("translations/en.json")
                        .bufferedReader()
                        .use { it.readText() }
            }

            val mappings = json.decodeFromString<Map<String, String>>(jsonString)
            logd("Loaded $localeCode (${mappings.size})")
            return JsonResolver(mappings)
        }
    }
}
