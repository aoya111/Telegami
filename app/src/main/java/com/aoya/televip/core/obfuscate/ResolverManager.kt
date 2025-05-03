package com.aoya.televip.core.obfuscate

object ResolverManager {
    private lateinit var translation: Resolver

    fun init(packageName: String) {
        translation =
            when (packageName) {
                "tw.nekomimi.nekogram" -> Nekogram
                else -> Default
            }
    }

    fun get(className: String): String = translation.get(className)

    fun getMethod(
        className: String,
        methodName: String,
    ): String = translation.getMethod(className, methodName)

    fun getField(
        className: String,
        fieldName: String,
    ): String = translation.getField(className, fieldName)
}
