package com.aoya.telegami.hooks

import com.aoya.telegami.core.Config
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.MessageHelper
import com.aoya.telegami.virt.messenger.AndroidUtilities
import com.aoya.telegami.virt.messenger.MessageObject
import com.aoya.telegami.virt.ui.ChatActivity
import com.aoya.telegami.virt.ui.actionbar.Theme
import com.aoya.telegami.virt.ui.cells.ChatMessageCell
import kotlin.math.ceil

class MarkMessages : Hook("MarkMessages") {
    private var deleteDrawableWidth: Int = 0
    private var editDrawableWidth: Int = 0

    override fun init() {
        deleteDrawableWidth = getDrawableResource("msg_delete")?.getIntrinsicWidth() ?: 0
        editDrawableWidth = getDrawableResource("msg_edit")?.getIntrinsicWidth() ?: 0

        findAndHook("org.telegram.ui.ChatActivity", "createView", HookStage.AFTER, filter = { true }) { param ->
            val o = ChatActivity(param.thisObject())
            Globals.loadDeletedMessagesForDialog(o.dialogId)
        }

        findAndHook("org.telegram.ui.Cells.ChatMessageCell", "measureTime", HookStage.AFTER, filter = {
            Config.isFeatureEnabled("MarkMessagesDeleted") ||
                Config.isFeatureEnabled("MarkMessagesEdited")
        }) { param ->
            val msgCell = ChatMessageCell(param.thisObject())
            val msgObj = MessageObject(param.arg<Any>(0))
            val dialogId = msgObj.dialogId
            val mid = msgObj.id

            var timeStr = msgCell.currentTimeString
            var timeTextWidth = msgCell.timeTextWidth
            var timeWidth = msgCell.timeWidth
            var customDrawableWidth = 0

            var isDeleted = false
            val oldWidth = ceil(Theme.chatTimePaint.measureText(timeStr, 0, timeStr.length)).toInt()
            if (Config.isFeatureEnabled("MarkMessagesDeleted")) {
                isDeleted = Globals.isDeletedMessage(dialogId, mid)
                if (isDeleted) {
                    val msg = Globals.getDeletedMessage(dialogId, mid)!!
                    timeStr = MessageHelper.createDeletedString(msg)
                    val newWidth = ceil(Theme.chatTimePaint.measureText(timeStr, 0, timeStr.length)).toInt()

                    val dwidth = newWidth - oldWidth
                    if (dwidth != 0) {
                        customDrawableWidth = deleteDrawableWidth
                        if (customDrawableWidth != 0) {
                            val drawableAdjustment =
                                customDrawableWidth * (Theme.chatTimePaint.textSize - AndroidUtilities.dp(2.0f)) / customDrawableWidth
                            timeTextWidth += drawableAdjustment.toInt() + dwidth
                            timeWidth += drawableAdjustment.toInt() + 5 * dwidth / 6
                        }
                    }
                }
            }
            if (!isDeleted) {
                if (Config.isFeatureEnabled("MarkMessagesEdited")) {
                    timeStr = MessageHelper.replaceWithIcon(timeStr)
                    val newWidth = ceil(Theme.chatTimePaint.measureText(timeStr, 0, timeStr.length)).toInt()

                    val dwidth = newWidth - oldWidth
                    if (dwidth != 0) {
                        customDrawableWidth = editDrawableWidth

                        timeTextWidth = msgCell.timeTextWidth
                        if (customDrawableWidth != 0) {
                            val drawableAdjustment =
                                customDrawableWidth * (Theme.chatTimePaint.textSize - AndroidUtilities.dp(2.0f)) / customDrawableWidth
                            timeTextWidth += drawableAdjustment.toInt() + dwidth
                            timeWidth += drawableAdjustment.toInt() + 5 * dwidth / 6
                        }
                    }
                }
            }
            msgCell.currentTimeString = timeStr
            msgCell.timeTextWidth = timeTextWidth
            msgCell.timeWidth = timeWidth
        }
    }
}
