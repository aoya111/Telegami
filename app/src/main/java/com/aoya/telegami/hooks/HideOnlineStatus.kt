package com.aoya.telegami.hooks

import com.aoya.telegami.core.Config
import com.aoya.telegami.virt.tgnet.tl.TLAccount
import com.aoya.telegami.virt.ui.ProfileActivity
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.i18n.TranslationManager as i18n
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object HideOnlineStatus : YukiBaseHooker() {
    const val CONNECTIONS_MANAGER_CN = "org.telegram.tgnet.ConnectionsManager"
    const val PROFILE_ACTIVITY_CN = "org.telegram.ui.ProfileActivity"
    val connectionsManagerClass by lazyClass(resolver.get(CONNECTIONS_MANAGER_CN))
    val profileActivityClass by lazyClass(resolver.get(PROFILE_ACTIVITY_CN))

    override fun onHook() {
        if (!Config.isFeatureEnabled("HideOnlineStatus")) return

        val tLAccountUpdateStatusClass = resolver.get("org.telegram.tgnet.tl.TL_account\$updateStatus").toClass()

        connectionsManagerClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(CONNECTIONS_MANAGER_CN, "sendRequestInternal")
            }.hook {
                before {
                    val req = args[0]
                    if (tLAccountUpdateStatusClass.isInstance(req)) {
                        TLAccount.UpdateStatus(req!!).offline = true
                    }
                }
            }

        profileActivityClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(PROFILE_ACTIVITY_CN, "updateProfileData")
            }.hook {
                after {
                    val o = ProfileActivity(instance)

                    val clientUserId = o.getUserConfig()?.getClientUserId() ?: 0L
                    val userId = o.userId

                    if (clientUserId != 0L && userId != 0L && userId == clientUserId) {
                        o.onlineTextView.getOrNull(1)?.setText(i18n.get("ProfileStatusOffline"))
                    }
                }
            }
    }
}
