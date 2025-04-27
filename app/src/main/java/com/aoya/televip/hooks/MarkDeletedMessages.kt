package com.aoya.televip.hooks

import android.text.SpannableStringBuilder
import com.aoya.televip.TeleVip
import com.aoya.televip.core.Config
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setObjectField
import com.aoya.televip.core.i18n.TranslationManager as i18n

class MarkDeletedMessages :
    Hook(
        "mark_deleted_messages",
        "Mark 'Deleted' messages",
    ) {
    override fun init() {
        findClass("org.telegram.ui.Cells.ChatMessageCell").hook("measureTime", HookStage.AFTER) { param ->
            val forwardingMessage = param.arg<Any>(0)
            val msgOwner = getObjectField(forwardingMessage, "messageOwner") ?: return@hook
            val peerId = getObjectField(msgOwner, "peer_id") ?: return@hook
            var userId = getLongField(peerId, "user_id")
            val chatId = getLongField(peerId, "chat_id")
            val channelId = getLongField(peerId, "channel_id")
            val id =
                if (userId != 0L) {
                    userId
                } else {
                    if (chatId != 0L) {
                        chatId
                    } else {
                        channelId
                    }
                }
            try {
                val msgIds = Config.getDeletedMessages(id)
                for (msgId in msgIds) {
                    if (msgId == getIntField(msgOwner, "id")) {
                        val delMsg =
                            if (TeleVip.packageName in listOf("uz.unnarsx.cherrygram", "com.exteragram.messenger")) {
                                SpannableStringBuilder(i18n.get("deleted"))
                            } else {
                                i18n.get("deleted")
                            }
                        setObjectField(param.thisObject(), "currentTimeString", delMsg)
                    }
                }
            } catch (e: Exception) {
                XposedBridge.log("Error parsing messages: ${e.message}")
            }
        }
    }
}
