package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.core.Config
import com.aoya.telegami.data.DeletedMessage
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.MessageHelper
import com.aoya.telegami.virt.messenger.AndroidUtilities
import com.aoya.telegami.virt.messenger.MessageObject
import com.aoya.telegami.virt.ui.ChatActivity
import com.aoya.telegami.virt.ui.actionbar.Theme
import com.aoya.telegami.virt.ui.cells.ChatMessageCell
import kotlinx.coroutines.launch
import com.aoya.telegami.core.i18n.TranslationManager as i18n

class MarkDeletedMessages :
    Hook(
        "mark_deleted_messages",
        "Mark 'Deleted' messages",
    ) {
    private val deletedMessagesCache = mutableMapOf<Pair<Long, Int>, DeletedMessage>()

    override fun init() {
        findAndHook("org.telegram.ui.ChatActivity", "createView", HookStage.AFTER, filter = { true }) { param ->
            val o = ChatActivity(param.thisObject())
            Globals.coroutineScope.launch {
                val deleted = db.deletedMessageDao().getAllForDialog(o.dialogId)
                deleted.forEach { msg ->
                    deletedMessagesCache[o.dialogId to msg.id] = msg
                }
            }
        }

        findAndHook("org.telegram.ui.Cells.ChatMessageCell", "measureTime", HookStage.AFTER, filter = {
            Config.isEnabled("show_deleted_messages")
        }) { param ->
            val msgCell = ChatMessageCell(param.thisObject())
            val msgObj = MessageObject(param.arg<Any>(0))
            val dialogId = msgObj.getDialogId()
            val mid = msgObj.getId()
            val msg = deletedMessagesCache[dialogId to mid] ?: return@findAndHook

            val timeStr = MessageHelper.createDeletedString(msg)
            val customDrawableWidth = getDrawableResource("msg_delete")?.mutate()?.getIntrinsicWidth() ?: 0

            var timeTextWidth = msgCell.timeTextWidth
            if (customDrawableWidth != 0) {
                val drawableAdjustment =
                    customDrawableWidth * (Theme.chatTimePaint.textSize - AndroidUtilities.dp(2.0f)) / customDrawableWidth
                timeTextWidth += drawableAdjustment.toInt()
            }
            msgCell.currentTimeString = timeStr
            if (Telegami.packageName == "tw.nekomimi.nekogram") {
                msgCell.timeTextWidth = timeTextWidth + AndroidUtilities.dp(6.0f)
                msgCell.timeWidth = timeTextWidth - AndroidUtilities.dp(6.0f)
            } else {
                msgCell.timeTextWidth = timeTextWidth
                msgCell.timeWidth = timeTextWidth
            }
        }
    }
}
