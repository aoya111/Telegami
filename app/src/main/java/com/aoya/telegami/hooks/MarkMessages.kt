package com.aoya.telegami.hooks

import android.text.SpannableStringBuilder
import com.aoya.telegami.Telegami
import com.aoya.telegami.core.Config
import com.aoya.telegami.data.DeletedMessage
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.MessageHelper
import com.aoya.telegami.virt.messenger.AndroidUtilities
import com.aoya.telegami.virt.messenger.MessageObject
import com.aoya.telegami.virt.tgnet.TLRPC
import com.aoya.telegami.virt.ui.ChatActivity
import com.aoya.telegami.virt.ui.actionbar.Theme
import com.aoya.telegami.virt.ui.cells.ChatMessageCell
import kotlinx.coroutines.launch
import kotlin.math.ceil
import com.aoya.telegami.core.i18n.TranslationManager as i18n

class MarkMessages :
    Hook(
        "mark_messages",
        "Mark messages",
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

        findAndHook("org.telegram.ui.Cells.ChatMessageCell", "measureTime", HookStage.AFTER, filter = { true }) { param ->
            val msgCell = ChatMessageCell(param.thisObject())
            val msgObj = MessageObject(param.arg<Any>(0))
            val dialogId = msgObj.getDialogId()
            val mid = msgObj.getId()
            val msgKey = dialogId to mid

            var timeStr = msgCell.currentTimeString
            var timeTextWidth = msgCell.timeTextWidth
            var timeWidth = msgCell.timeWidth
            var customDrawableWidth = 0

            var isDeleted = false
            val oldWidth = ceil(Theme.chatTimePaint.measureText(timeStr, 0, timeStr.length)).toInt()
            if (Config.isHookEnabled("show_deleted_messages")) {
                deletedMessagesCache[msgKey]?.let { msg ->
                    timeStr = MessageHelper.createDeletedString(msg)
                    val newWidth = ceil(Theme.chatTimePaint.measureText(timeStr, 0, timeStr.length)).toInt()

                    val dwidth = newWidth - oldWidth
                    if (dwidth != 0) {
                        customDrawableWidth = getDrawableResource("msg_delete")?.getIntrinsicWidth() ?: 0
                        if (customDrawableWidth != 0) {
                            val drawableAdjustment =
                                customDrawableWidth * (Theme.chatTimePaint.textSize - AndroidUtilities.dp(2.0f)) / customDrawableWidth
                            timeTextWidth += drawableAdjustment.toInt() + dwidth
                            timeWidth += drawableAdjustment.toInt() + 5 * dwidth / 6
                        }
                        // timeWidth = timeTextWidth
                    }
                    isDeleted = true
                }
            }
            if (!isDeleted) {
                timeStr = MessageHelper.replaceWithIcon(timeStr)
                val newWidth = ceil(Theme.chatTimePaint.measureText(timeStr, 0, timeStr.length)).toInt()

                val dwidth = newWidth - oldWidth
                if (dwidth != 0) {
                    customDrawableWidth = getDrawableResource("msg_edit")?.getIntrinsicWidth() ?: 0

                    timeTextWidth = msgCell.timeTextWidth
                    if (customDrawableWidth != 0) {
                        val drawableAdjustment =
                            customDrawableWidth * (Theme.chatTimePaint.textSize - AndroidUtilities.dp(2.0f)) / customDrawableWidth
                        timeTextWidth += drawableAdjustment.toInt() + dwidth
                        timeWidth += drawableAdjustment.toInt() + 5 * dwidth / 6
                    }
                }
            }
            msgCell.currentTimeString = timeStr
            msgCell.timeTextWidth = timeTextWidth
            msgCell.timeWidth = timeWidth
        }
    }
}
