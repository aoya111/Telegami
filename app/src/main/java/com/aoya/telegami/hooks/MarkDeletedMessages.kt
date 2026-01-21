package com.aoya.telegami.hooks

import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.hook
import com.aoya.telegami.virt.messenger.LocaleController
import com.aoya.telegami.virt.messenger.MessageObject
import com.aoya.telegami.virt.ui.actionbar.Theme
import com.aoya.telegami.virt.ui.cells.ChatMessageCell
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.launch
import kotlin.math.ceil
import com.aoya.telegami.core.i18n.TranslationManager as i18n
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class MarkDeletedMessages :
    Hook(
        "mark_deleted_messages",
        "Mark 'Deleted' messages",
    ) {
    override fun init() {
        findClass(
            "org.telegram.ui.Cells.ChatMessageCell",
        ).hook(resolver.getMethod("org.telegram.ui.Cells.ChatMessageCell", "measureTime"), HookStage.AFTER) { param ->
            val msgCell = ChatMessageCell(param.thisObject())
            val msgObj = MessageObject(param.arg<Any>(0))
            val dialogId = msgObj.getDialogId()
            val mid = msgObj.getId()
            Globals.coroutineScope.launch {
                try {
                    val msg = db.deletedMessageDao().get(mid, dialogId) ?: return@launch

                    val delMsgStr = i18n.get("DeletedMessage")

                    var timeStr = delMsgStr
                    msg.createdAt?.let {
                        val dayFormatter = LocaleController.getInstance().getFormatterDay()
                        timeStr += " " + dayFormatter.format(it * 1000L)
                    }
                    val timeTextWidth =
                        ceil(Theme.chatTimePaint.measureText(timeStr, 0, timeStr.length)).toInt()
                    msgCell.currentTimeString = timeStr
                    msgCell.timeTextWidth = timeTextWidth
                    msgCell.timeWidth = timeTextWidth
                } catch (e: Exception) {
                    XposedBridge.log("Error parsing messages: ${e.message}")
                }
            }
        }
    }
}
