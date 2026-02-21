package com.aoya.telegami.hooks

import com.aoya.telegami.core.i18n.TranslationManager
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.messenger.LocaleController

class LocaleController :
    Hook(
        "LocaleController",
        "locale controller",
    ) {
    override fun init() {
        findAndHook("org.telegram.messenger.LocaleController", "applyLanguage", HookStage.AFTER, filter = { true }) { param ->
            val localeInfo = LocaleController.LocaleInfo(param.arg<Any>(0))
            TranslationManager.reloadTranslations(localeInfo.shortName)
        }
    }
}
