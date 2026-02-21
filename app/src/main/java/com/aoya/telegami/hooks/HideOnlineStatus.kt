package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.logd
import com.aoya.telegami.virt.tgnet.tl.TLAccount
import com.aoya.telegami.virt.ui.ProfileActivity
import com.aoya.telegami.core.i18n.TranslationManager as i18n

class HideOnlineStatus :
    Hook(
        "hide_online_status",
        "Hide 'Online' status",
    ) {
    private val updateStatusClass: Class<*> by lazy {
        findClass("org.telegram.tgnet.tl.TL_account\$updateStatus")
    }

    override fun init() {
        findAndHook("org.telegram.tgnet.ConnectionsManager", "sendRequestInternal", HookStage.BEFORE) { param ->
            val req = param.arg<Any>(0)
            if (updateStatusClass.isInstance(req)) {
                TLAccount.UpdateStatus(param.arg<Any>(0)).offline = true
            }
        }

        findAndHook("org.telegram.tgnet.ConnectionsManager", "sendRequestInternal", HookStage.AFTER) { param ->
            val req = param.arg<Any>(0)
            if (updateStatusClass.isInstance(req)) {
                logd("[HideOnlineStatus] isOffline = ${TLAccount.UpdateStatus(param.arg<Any>(0)).offline}")
            }
        }

        findAndHook("org.telegram.ui.ProfileActivity", "updateProfileData", HookStage.AFTER) { param ->
            val o = ProfileActivity(param.thisObject())

            val clientUserId = o.getUserConfig().clientUserId
            val userId = o.userId

            if (clientUserId != 0L && userId != 0L && userId == clientUserId) {
                o.onlineTextView.getOrNull(1)?.setText(i18n.get("ProfileStatusOffline"))
            }
        }
    }
}
