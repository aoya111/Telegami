package com.aoya.telegami.hooks

import android.util.Log
import com.aoya.telegami.Telegami
import com.aoya.telegami.util.findMethod
import com.aoya.telegami.service.Config
import com.aoya.telegami.util.MessageHelper
import com.aoya.telegami.virt.messenger.AndroidUtilities
import com.aoya.telegami.virt.messenger.MessageObject
import com.aoya.telegami.virt.ui.ChatActivity
import com.aoya.telegami.virt.ui.actionbar.Theme
import com.aoya.telegami.virt.ui.cells.ChatMessageCell
import io.github.libxposed.api.XposedInterface
import kotlin.math.ceil
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object MarkMessages {
    const val CHAT_ACTIVITY_CN = "org.telegram.ui.ChatActivity"
    const val CHAT_MESSAGE_CELL_CN = "org.telegram.ui.Cells.ChatMessageCell"

    val isMarkDeletedEnabled: Boolean by lazy {
        Config.isFeatureEnabled("MarkMessagesDeleted")
    }
    val isMarkEditedEnabled: Boolean by lazy {
        Config.isFeatureEnabled("MarkMessagesEdited")
    }
    val isAnyMarkEnabled: Boolean by lazy {
        isMarkDeletedEnabled || isMarkEditedEnabled
    }

    val deleteDrawableWidth: Int by lazy {
        Telegami.getDrawableResource("msg_delete")?.let {
            it?.getIntrinsicWidth()
        } ?: 0
    }
    val editDrawableWidth: Int by lazy {
        Telegami.getDrawableResource("msg_edit")?.let {
            it?.getIntrinsicWidth()
        } ?: 0
    }

    fun install(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        val createView = classLoader.findMethod(CHAT_ACTIVITY_CN, "createView", 1)
        xposed.hook(createView).intercept { chain ->
            val args = chain.args.toMutableList()
            val result = chain.proceed(args.toTypedArray())
            try {
                val o = ChatActivity(chain.thisObject!!)
                Globals.loadDeletedMessagesForDialog(o.dialogId)
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed after ${createView.name}", throwable)
            }
            result
        }
        if (!isAnyMarkEnabled) return
        val measureTime = classLoader.findMethod(CHAT_MESSAGE_CELL_CN, "measureTime")
        xposed.hook(measureTime).intercept { chain ->
            val args = chain.args.toMutableList()
            val result = chain.proceed(args.toTypedArray())
            try {
                val msgCell = ChatMessageCell(chain.thisObject!!)
                val msgObj = MessageObject(args[0]!!)
                val dialogId = msgObj.getDialogId()
                val mid = msgObj.getId()

                var timeStr = msgCell.currentTimeString
                var timeTextWidth = msgCell.timeTextWidth
                var timeWidth = msgCell.timeWidth
                var customDrawableWidth = 0

                var isDeleted = false
                val oldWidth = ceil(Theme.chatTimePaint.measureText(timeStr, 0, timeStr.length)).toInt()
                if (isMarkDeletedEnabled) {
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
                                    customDrawableWidth * (Theme.chatTimePaint.textSize - AndroidUtilities.dp(2.0f)) /
                                        customDrawableWidth
                                timeTextWidth += drawableAdjustment.toInt() + dwidth
                                timeWidth += drawableAdjustment.toInt() + 5 * dwidth / 6
                            }
                        }
                    }
                }
                if (!isDeleted) {
                    if (isMarkEditedEnabled) {
                        timeStr = MessageHelper.replaceWithIcon(timeStr)
                        val newWidth = ceil(Theme.chatTimePaint.measureText(timeStr, 0, timeStr.length)).toInt()

                        val dwidth = newWidth - oldWidth
                        if (dwidth != 0) {
                            customDrawableWidth = editDrawableWidth

                            timeTextWidth = msgCell.timeTextWidth
                            if (customDrawableWidth != 0) {
                                val drawableAdjustment =
                                    customDrawableWidth * (Theme.chatTimePaint.textSize - AndroidUtilities.dp(2.0f)) /
                                        customDrawableWidth
                                timeTextWidth += drawableAdjustment.toInt() + dwidth
                                timeWidth += drawableAdjustment.toInt() + 5 * dwidth / 6
                            }
                        }
                    }
                }
                msgCell.currentTimeString = timeStr
                msgCell.timeTextWidth = timeTextWidth
                msgCell.timeWidth = timeWidth
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed after ${measureTime.name}", throwable)
            }
            result
        }
    }
}
