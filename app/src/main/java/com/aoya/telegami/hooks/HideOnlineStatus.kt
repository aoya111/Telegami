package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.hook
import com.aoya.telegami.virt.tgnet.tl.TLAccount
import com.aoya.telegami.virt.ui.ProfileActivity
import com.aoya.telegami.core.i18n.TranslationManager as i18n
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class HideOnlineStatus :
    Hook(
        "hide_online_status",
        "Hide 'Online' status",
    ) {
    override fun init() {
        findClass(
            "org.telegram.tgnet.ConnectionsManager",
        ).hook(resolver.getMethod("org.telegram.tgnet.ConnectionsManager", "sendRequestInternal"), HookStage.BEFORE) { param ->
            val tlAccountUpdateStatusClass = findClass("org.telegram.tgnet.tl.TL_account\$updateStatus")

            if (tlAccountUpdateStatusClass.isInstance(param.arg<Any>(0))) {
                TLAccount.UpdateStatus(param.arg<Any>(0)).offline = false
            }
        }

        findClass("org.telegram.ui.ProfileActivity")
            .hook(resolver.getMethod("org.telegram.ui.ProfileActivity", "updateProfileData"), HookStage.AFTER) { param ->
                val o = ProfileActivity(param.thisObject())

                val clientUserId = o.getUserConfig().getClientUserId()
                val userId = o.userId

                if (clientUserId != 0L && userId != 0L && userId == clientUserId) {
                    o.onlineTextView[1].setText(i18n.get("ProfileStatusOffline"))
                }
            }
    }
}
