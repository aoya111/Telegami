package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setBooleanField
import com.aoya.televip.core.i18n.TranslationManager as i18n
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class AddChatNavigation :
    Hook(
        "add_chat_navigation",
        "Add chat navigation",
    ) {
    override fun init() {
        val scrollToMessageId = 500

        findClass(
            "org.telegram.ui.ChatActivity",
        ).hook(resolver.getMethod("org.telegram.ui.ChatActivity", "createView"), HookStage.AFTER) { param ->
            val o = param.thisObject()
            val headerItem = getObjectField(o, "headerItem") ?: return@hook

            callMethod(
                headerItem,
                resolver.getMethod("org.telegram.ui.ActionBar.ActionBarMenuItem", "lazilyAddColoredGap"),
            )

            callMethod(
                headerItem,
                resolver.getMethod("org.telegram.ui.ActionBar.ActionBarMenuItem", "lazilyAddSubItem"),
                scrollToMessageId,
                getDrawableResource("msg_go_up"),
                i18n.get("chat_scroll_to_top"),
            )
        }

        val suffix = "13"
        findClass(
            "org.telegram.ui.ChatActivity\$$suffix",
        ).hook(resolver.getMethod("org.telegram.ui.ChatActivity\$$suffix", "onItemClick"), HookStage.AFTER) { param ->
            val id = param.arg<Int>(0)
            val chatActivity = getObjectField(param.thisObject(), "this$0")
            if (id == scrollToMessageId) {
                callMethod(
                    chatActivity,
                    resolver.getMethod("org.telegram.ui.ChatActivity", "scrollToMessageId"),
                    1,
                    0,
                    true,
                    0,
                    true,
                    0,
                )
                setBooleanField(
                    chatActivity,
                    resolver.getMethod("org.telegram.ui.ChatActivity", "canShowPagedownButton"),
                    true,
                )
                callMethod(
                    chatActivity,
                    resolver.getMethod("org.telegram.ui.ChatActivity", "updatePagedownButtonVisibility"),
                    true,
                )
                setBooleanField(
                    chatActivity,
                    resolver.getMethod("org.telegram.ui.ChatActivity", "pagedownButtonShowedByScroll"),
                    true,
                )
            }
        }
    }
}
