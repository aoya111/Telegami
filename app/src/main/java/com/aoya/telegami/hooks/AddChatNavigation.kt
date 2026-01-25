package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.ui.ChatActivity
import com.aoya.telegami.virt.ui.actionbar.ActionBarMenuItem
import de.robv.android.xposed.XposedHelpers.getObjectField
import com.aoya.telegami.core.i18n.TranslationManager as i18n

class AddChatNavigation :
    Hook(
        "add_chat_navigation",
        "Add chat navigation",
    ) {
    override fun init() {
        val scrollToMessageId = 500

        findAndHook("org.telegram.ui.ChatActivity", "createView", HookStage.AFTER, filter = { true }) { param ->
            val o = param.thisObject()
            val headerItem = ActionBarMenuItem(getObjectField(o, "headerItem") ?: return@findAndHook)

            headerItem.lazilyAddColoredGap()

            headerItem.lazilyAddSubItem(
                scrollToMessageId,
                getDrawableResource("msg_go_up"),
                i18n.get("chat_scroll_to_top"),
            )
        }

        val suffix = "13"
        findAndHook("org.telegram.ui.ChatActivity\$$suffix", "onItemClick", HookStage.AFTER, filter = { true }) { param ->
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
