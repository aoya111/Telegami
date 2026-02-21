package com.aoya.telegami.core.i18n

import android.content.Context
import android.os.Build
import com.aoya.telegami.utils.logd

object TranslationManager {
    private lateinit var translation: Translation
    private var modulePath: String = ""

    fun init(
        context: Context,
        modulePath: String,
    ) {
        this.modulePath = modulePath

        val locale =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }
        translation = JsonResolver.fromModuleAssets(modulePath, locale.language)
    }

    fun reloadTranslations(languageCode: String) {
        translation = JsonResolver.fromModuleAssets(modulePath, languageCode)
        logd("Reloaded translations for: $languageCode")
    }

    fun get(key: String): String = translation.get(key)
}
