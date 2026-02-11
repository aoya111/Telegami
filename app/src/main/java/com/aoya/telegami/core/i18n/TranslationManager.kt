package com.aoya.telegami.core.i18n

import android.content.Context
import android.os.Build

object TranslationManager {
    private lateinit var translation: Translation

    fun init(
        context: Context,
        modulePath: String,
    ) {
        val locale =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }
        translation = JsonResolver.fromModuleAssets(modulePath, locale.language)
    }

    fun get(key: String): String = translation.get(key)
}
