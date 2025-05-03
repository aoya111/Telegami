package com.aoya.televip.core.obfuscate

interface Resolver {
    fun get(className: String): String

    fun getMethod(
        className: String,
        methodName: String,
    ): String

    fun getField(
        className: String,
        fieldName: String,
    ): String
}

object Default : Resolver {
    override fun get(className: String): String = className

    override fun getMethod(
        className: String,
        methodName: String,
    ): String = methodName

    override fun getField(
        className: String,
        fieldName: String,
    ): String = fieldName
}
