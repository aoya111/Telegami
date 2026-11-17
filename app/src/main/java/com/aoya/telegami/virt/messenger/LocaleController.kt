package com.aoya.telegami.virt.messenger

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.messenger.time.FastDateFormat
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class LocaleController(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    fun getFormatterDay(): FastDateFormat =
        FastDateFormat(
            instance.asResolver().firstMethod { this.name = resolver.getMethod(objPath, "getFormatterDay"); parameters() }.invoke()!!,
        )

    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.LocaleController"

        fun getInstance(): LocaleController =
            LocaleController(
                (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>).resolve().firstMethod { this.name = resolver.getMethod(OBJ_PATH, "getInstance"); parameters() }.invoke()!!,
            )

        fun getString(
            id: Int,
            fallback: String? = null,
        ): String =
            if (Telegami.packageName == "tw.nekomimi.nekogram") {
                val result = getStringWithFallback(id, fallback)
                if (result.startsWith("LOC_ERR:")) fallback ?: "" else result
            } else {
                // 13.2.2 no longer exposes string names in resources.arsc, so
                // name-based lookups return 0. Honour the caller's fallback
                // instead of asking Telegram for resource 0 (LOC_ERR:null).
                if (id == 0) return fallback ?: ""
                val result =
                    (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>)
                        .resolve()
                        .firstMethod {
                            this.name = resolver.getMethod(OBJ_PATH, "getString")
                            parameters(*arrayOf(id.javaClass))
                        }.invoke(id)!! as String
                if (result.startsWith("LOC_ERR:")) fallback ?: "" else result
            }

        private fun getStringWithFallback(
            id: Int,
            fallback: String?,
        ): String =
            (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>)
                .resolve()
                .firstMethod {
                    this.name = resolver.getMethod(OBJ_PATH, "getStringWithFallback")
                    parameters(Int::class.javaPrimitiveType!!, String::class.java)
                }.invoke(id, fallback)!! as String
    }

    class LocaleInfo(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.messenger.LocaleController\$LocaleInfo"

        val shortName: String
            get() = instance.asResolver().firstField { this.name = resolver.getField(objPath, "shortName") }.get()!! as String
    }
}
