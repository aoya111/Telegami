package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setBooleanField
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

                val obj = param.arg<Any>(0)
                if (tlAccountUpdateStatusClass.isInstance(obj)) {
                    setBooleanField(obj, "offline", true)
                }
            } catch (e: Exception) {
                XposedBridge.log("Error while handling sendRequestInternal: ${e.message}")
            }
        }

        findClass("org.telegram.ui.ProfileActivity")
            .hook(resolver.getMethod("org.telegram.ui.ProfileActivity", "updateProfileData"), HookStage.AFTER) { param ->
                val o = param.thisObject()

                try {
                    val userConfig = callMethod(o, resolver.getMethod("org.telegram.ui.ActionBar.BaseFragment", "getUserConfig"))

                    val clientUserId =
                        callMethod(
                            userConfig,
                            resolver.getMethod("org.telegram.messenger.UserConfig", "getClientUserId"),
                        ) as? Long ?: return@hook

                    val userId = getLongField(o, "userId")

                    if (clientUserId != 0L && userId != 0L && userId == clientUserId) {
                        val onlineTextViewArray =
                            getObjectField(o, "onlineTextView") as? Array<*>
                        if (onlineTextViewArray != null && onlineTextViewArray.size > 1) {
                            val simpleTextView1 = onlineTextViewArray[1]
                            callMethod(
                                simpleTextView1,
                                resolver.getMethod("org.telegram.ui.ActionBar.SimpleTextView", "setText"),
                                i18n.get("offline_status"),
                            )
                        }
                    }
                } catch (e: Exception) {
                    XposedBridge.log("Error while handling updateProfileData: ${e.message}")
                }
            }
    }
}
