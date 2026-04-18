package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.service.Config
import com.aoya.telegami.util.MessageHelper
import com.aoya.telegami.virt.messenger.AndroidUtilities
import com.aoya.telegami.virt.messenger.MessageObject
import com.aoya.telegami.virt.ui.ChatActivity
import com.aoya.telegami.virt.ui.actionbar.Theme
import com.aoya.telegami.virt.ui.cells.ChatMessageCell
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.VagueType
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import kotlin.math.ceil
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object MarkMessages : YukiBaseHooker() {
    const val CHAT_ACTIVITY_CN = "org.telegram.ui.ChatActivity"
    const val CHAT_MESSAGE_CELL_CN = "org.telegram.ui.Cells.ChatMessageCell"
    val chatActivityClass by lazyClass(resolver.get(CHAT_ACTIVITY_CN))
    val chatMessageCellClass by lazyClass(resolver.get(CHAT_MESSAGE_CELL_CN))

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

    override fun onHook() {
        chatActivityClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(CHAT_ACTIVITY_CN, "createView")
                parameters(VagueType)
            }.hook {
                after {
                    val o = ChatActivity(instance!!)
                    Globals.loadDeletedMessagesForDialog(o.dialogId)
                }
            }
        if (!isAnyMarkEnabled) return
        resolver
            .get(CHAT_MESSAGE_CELL_CN)
            .toClass()
            .resolve()
            .firstMethod {
                name = resolver.getMethod(CHAT_MESSAGE_CELL_CN, "measureTime")
            }.hook {
                after {
                    val msgCell = ChatMessageCell(instance!!)
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
                }
            }
    }
}
