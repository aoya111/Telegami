package com.aoya.telegami.virt.messenger

import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.messenger.time.FastDateFormat
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class LocaleController(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    fun getFormatterDay(): FastDateFormat =
        FastDateFormat(
            callMethod(
                instance,
                resolver.getMethod(objPath, "getFormatterDay"),
            ),
        )

    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.LocaleController"

        fun getInstance(): LocaleController =
            LocaleController(
                callStaticMethod(
                    Telegami.loadClass(resolver.get(OBJ_PATH)),
                    resolver.getMethod(OBJ_PATH, "getInstance"),
                ),
            )

        fun getString(id: Int): String =
            callStaticMethod(
                Telegami.loadClass(resolver.get(OBJ_PATH)),
                resolver.getMethod(OBJ_PATH, "getString"),
                id,
            ) as String
    }
}
