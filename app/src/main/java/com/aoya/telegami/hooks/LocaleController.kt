package com.aoya.telegami.hooks

import com.aoya.telegami.core.i18n.TranslationManager
import com.aoya.telegami.virt.messenger.LocaleController
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object LocaleController : YukiBaseHooker() {
    const val LOCALE_CONTROLLER_CN = "org.telegram.messenger.LocaleController"

    val localeControllerClass by lazyClass(resolver.get(LOCALE_CONTROLLER_CN))

    override fun onHook() {
        localeControllerClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(LOCALE_CONTROLLER_CN, "applyLanguage")
            }.hook {
                after {
                    val localeInfo = args[0]?.let { LocaleController.LocaleInfo(it) } ?: return@after
                    TranslationManager.reloadTranslations(localeInfo.shortName)
                }
            }
    }
}
