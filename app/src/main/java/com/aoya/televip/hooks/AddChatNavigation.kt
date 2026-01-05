package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import com.aoya.televip.virt.ui.ChatActivity
import com.aoya.televip.virt.ui.actionbar.ActionBarMenuItem
import de.robv.android.xposed.XposedHelpers.getObjectField
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
            val headerItem = ActionBarMenuItem(getObjectField(o, "headerItem") ?: return@hook)

            headerItem.lazilyAddColoredGap()

            headerItem.lazilyAddSubItem(
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
            val chatActivity = ChatActivity(getObjectField(param.thisObject(), "this$0"))
            if (id == scrollToMessageId) {
                chatActivity.scrollToMessageId(1, 0, true, 0, true, 0)
                chatActivity.canShowPagedownButton = true
                chatActivity.updatePagedownButtonVisibility(true)
                chatActivity.pagedownButtonShowedByScroll = true
            }
        }
    }
}
