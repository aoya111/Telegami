package com.aoya.telegami.core.obfuscate

import com.aoya.telegami.core.ModuleAssets
import com.aoya.telegami.util.logd
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class MappingEntry(
    val obfuscated: String,
    val hint: String? = null,
)

@Serializable
data class ClassMapping(
    val obfuscated: String,
    val hint: String? = null,
    val methods: Map<String, MappingEntry> = emptyMap(),
    val fields: Map<String, MappingEntry> = emptyMap(),
)

@Serializable
data class ObfuscationMappings(
    val variant: String,
    @SerialName("package_name")
    val packageName: String,
    val version: String? = null,
    val updated: String? = null,
    val mappings: Map<String, ClassMapping> = emptyMap(),
)

class JsonResolver(
    private val mappings: ObfuscationMappings,
) : Resolver {
    override fun get(className: String): String = mappings.mappings[className]?.obfuscated ?: className

    override fun getMethod(
        className: String,
        methodName: String,
    ): String =
        mappings.mappings[className]
            ?.methods
            ?.get(methodName)
            ?.obfuscated ?: methodName

    override fun getField(
        className: String,
        fieldName: String,
    ): String =
        mappings.mappings[className]
            ?.fields
            ?.get(fieldName)
            ?.obfuscated ?: fieldName

    companion object {
        private val json =
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }

        fun fromModuleAssets(
            modulePath: String,
            variantName: String,
        ): JsonResolver {
            val variant = variantName.lowercase()
            val jsonString =
                ModuleAssets.open(modulePath, "obfuscation_mappings/$variant.json")
                    .bufferedReader()
                    .use { it.readText() }

            val mappings = json.decodeFromString<ObfuscationMappings>(jsonString)
            logd("Loaded $variant [version=${mappings.version}] (${mappings.mappings.size} classes)")
            return JsonResolver(mappings)
        }
    }
}
