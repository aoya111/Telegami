package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import com.aoya.televip.virt.messenger.LocaleController
import com.aoya.televip.virt.messenger.MessageObject
import com.aoya.televip.virt.ui.actionbar.Theme
import com.aoya.televip.virt.ui.cells.ChatMessageCell
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.ceil
import com.aoya.televip.core.i18n.TranslationManager as i18n
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class MarkDeletedMessages :
    Hook(
        "mark_deleted_messages",
        "Mark 'Deleted' messages",
    ) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun init() {
        findClass(
            "org.telegram.ui.Cells.ChatMessageCell",
        ).hook(resolver.getMethod("org.telegram.ui.Cells.ChatMessageCell", "measureTime"), HookStage.AFTER) { param ->
            val msgCell = ChatMessageCell(param.thisObject())
            val msgObj = MessageObject(param.arg<Any>(0))
            val dialogId = msgObj.getDialogId()
            try {
                runBlocking {
                    launch {
                        val msgs = db.deletedMessageDao().getAllForDialog(dialogId)
                        val mid = msgObj.getId()

                        msgs.find { it.id == mid }?.let { msg ->
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
                        }
                    }.join()
                }
            } catch (e: Exception) {
                XposedBridge.log("Error parsing messages: ${e.message}")
            }
        }
    }
}
