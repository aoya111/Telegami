package com.aoya.telegami.hooks

import com.aoya.telegami.core.i18n.TranslationManager
import com.aoya.telegami.virt.messenger.LocaleController
import com.aoya.telegami.util.findMethod
import android.util.Log
import io.github.libxposed.api.XposedInterface

object LocaleController {
    const val LOCALE_CONTROLLER_CN = "org.telegram.messenger.LocaleController"

    fun install(xposed: XposedInterface, classLoader: ClassLoader) {
        val method = classLoader.findMethod(LOCALE_CONTROLLER_CN, "applyLanguage")
        xposed.hook(method).intercept { chain ->
            val args = chain.args.toMutableList()
            val result = chain.proceed(args.toTypedArray())
            try {
                run after@ {
                    val localeInfo = args[0]?.let { LocaleController.LocaleInfo(it) } ?: return@after
                    TranslationManager.reloadTranslations(localeInfo.shortName)
                }
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed after ${method.name}", throwable)
            }
            result
        }
    }
}
