package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import com.aoya.televip.virt.tgnet.tl.TLAccount
import com.aoya.televip.virt.ui.ProfileActivity
import de.robv.android.xposed.XposedBridge
import com.aoya.televip.core.i18n.TranslationManager as i18n
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class HideOnlineStatus :
    Hook(
        "hide_online_status",
        "Hide 'Online' status",
    ) {
    override fun init() {
        findClass(
            "org.telegram.tgnet.ConnectionsManager",
        ).hook(resolver.getMethod("org.telegram.tgnet.ConnectionsManager", "sendRequestInternal"), HookStage.BEFORE) { param ->
            try {
                val tlAccountUpdateStatusClass = findClass("org.telegram.tgnet.tl.TL_account\$updateStatus")

                if (tlAccountUpdateStatusClass.isInstance(param.arg<Any>(0))) {
                    TLAccount.UpdateStatus(param.arg<Any>(0)).offline = false
                }
            } catch (e: Exception) {
                XposedBridge.log("Error while handling sendRequestInternal: ${e.message}")
            }
        }

        findClass("org.telegram.ui.ProfileActivity")
            .hook(resolver.getMethod("org.telegram.ui.ProfileActivity", "updateProfileData"), HookStage.AFTER) { param ->
                val o = ProfileActivity(param.thisObject())

                try {
                    val clientUserId = o.getUserConfig().getClientUserId()
                    val userId = o.userId

                    if (clientUserId != 0L && userId != 0L && userId == clientUserId) {
                        o.onlineTextView[1].setText(i18n.get("offline_status"))
                    }
                } catch (e: Exception) {
                    XposedBridge.log("Error while handling updateProfileData: ${e.message}")
                }
            }
    }
}
