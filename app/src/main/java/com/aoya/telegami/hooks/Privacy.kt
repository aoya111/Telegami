package com.aoya.telegami.hooks

import com.aoya.telegami.service.Config
import com.aoya.telegami.util.logd
import com.aoya.telegami.virt.tgnet.tl.TLAccount
import com.aoya.telegami.virt.ui.ProfileActivity
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.i18n.TranslationManager as i18n
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object Privacy : YukiBaseHooker() {
    const val CONNECTIONS_MANAGER_CN = "org.telegram.tgnet.ConnectionsManager"
    const val PROFILE_ACTIVITY_CN = "org.telegram.ui.ProfileActivity"

    val connectionsManagerClass by lazyClass(resolver.get(CONNECTIONS_MANAGER_CN))
    val profileActivityClass by lazyClass(resolver.get(PROFILE_ACTIVITY_CN))

    val tlMessagesSetEncTypingClass = resolver.get("org.telegram.tgnet.TLRPC\$TL_messages_setEncryptedTyping").toClass()
    val tlMessagesSetTypingClass = resolver.get("org.telegram.tgnet.TLRPC\$TL_messages_setTyping").toClass()
    val tlAccountUpdateStatusClass = resolver.get("org.telegram.tgnet.tl.TL_account\$updateStatus").toClass()

    override fun onHook() {
        connectionsManagerClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(CONNECTIONS_MANAGER_CN, "sendRequestInternal")
            }.hook {
                before {
                    val req = args[0]

                    if ((tlMessagesSetTypingClass.isInstance(req) || tlMessagesSetEncTypingClass.isInstance(req)) &&
                        Config.isFeatureEnabled("HideTyping")
                    ) {
                        logd("[PrivacyHook] should HideTyping")
                        resultNull()
                    }

                    if (tlAccountUpdateStatusClass.isInstance(req) && Config.isFeatureEnabled("HideOnlineStatus")) {
                        logd("[PrivacyHook] should HideOnlineStatus")
                        req?.let { TLAccount.UpdateStatus(it).offline = true }
                    }
                }
            }

        if (Config.isFeatureEnabled("HideOnlineStatus")) {
            profileActivityClass
                .resolve()
                .firstMethod {
                    name = resolver.getMethod(PROFILE_ACTIVITY_CN, "updateProfileData")
                }.hook {
                    after {
                        val o = ProfileActivity(instance)

                        val clientUserId = o.getUserConfig().getClientUserId()
                        val userId = o.userId

                        if (clientUserId != 0L && userId != 0L && userId == clientUserId) {
                            o.onlineTextView.getOrNull(1)?.setText(i18n.get("ProfileStatusOffline"))
                        }
                    }
                }
        }
    }
}
