package com.aoya.televip.core.i18n

import android.content.Context
import android.os.Build

object TranslationManager {
    private lateinit var translation: Translation

    fun init(context: Context) {
        val locale =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }
        val localeCode = "${locale.language}_${locale.country}"

        translation =
            when (localeCode) {
                "ar" -> I18NarSA
                "ar_SA" -> I18NarSA
                "zh" -> I18NzhCN
                "zh_CN" -> I18NzhCN
                else -> I18NenUS
            }
    }

    fun get(key: String): String = translation.get(key)
}
