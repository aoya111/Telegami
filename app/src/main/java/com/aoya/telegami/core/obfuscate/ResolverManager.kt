package com.aoya.telegami.core.obfuscate

import com.aoya.telegami.utils.loge
import com.aoya.telegami.utils.logw

object ResolverManager {
    private lateinit var resolver: Resolver

    private val packageToVariant =
        mapOf(
            "tw.nekomimi.nekogram" to "nekogram",
            "xyz.nextalone.nagram" to "nagram",
        )

    fun init(
        packageName: String,
        modulePath: String,
    ) {
        resolver =
            try {
                val variantName = packageToVariant[packageName]
                if (variantName != null) {
                    JsonResolver.fromModuleAssets(modulePath, variantName)
                } else {
                    logw("Unknown package $packageName")
                    Default
                }
            } catch (e: Exception) {
                loge("Telegami: Failed to load mappings", e)
                Default
            }
    }

    fun get(className: String): String = resolver.get(className)

    fun getMethod(
        className: String,
        methodName: String,
    ): String = resolver.getMethod(className, methodName)

    fun getField(
        className: String,
        fieldName: String,
    ): String = resolver.getField(className, fieldName)
}
