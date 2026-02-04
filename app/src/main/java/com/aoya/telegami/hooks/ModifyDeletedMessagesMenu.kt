package com.aoya.telegami.hooks

import android.view.View
import android.widget.LinearLayout
import com.aoya.telegami.core.Config
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.ui.cells.ChatMessageCell
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getObjectField
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ModifyDeletedMessagesMenu :
    Hook(
        "modify_deleted_messages_menu",
        "Modify 'Deleted' messages menu",
    ) {
    var msgIsDeleted = false

    companion object {
        val SEEN_TYPE_SEEN = 0
    }

    override fun init() {
        findAndHook(
            "org.telegram.ui.ChatActivity",
            "lambda\$createMenu$290",
            HookStage.BEFORE,
            filter = {
                Config.isHookEnabled("show_deleted_messages")
            },
        ) { param ->
            if (!msgIsDeleted) return@findAndHook
            val o = param.thisObject()

            val scrimPopupWindow = getObjectField(o, "scrimPopupWindow")
            val scrimPopupContainerLayout = callMethod(scrimPopupWindow, "getContentView")

            val actionBarPopupWindowLayoutClass =
                findClass("org.telegram.ui.ActionBar.ActionBarPopupWindow\$ActionBarPopupWindowLayout")

            var popupLayout: Any? = null
            scrimPopupContainerLayout.let { l ->
                if (l is LinearLayout) {
                    val viewsToRemove = mutableListOf<View>()
                    val childCount = l.childCount
                    for (i in 0 until childCount) {
                        val child = l.getChildAt(i)
                        if (actionBarPopupWindowLayoutClass.isInstance(child)) {
                            popupLayout = child
                            break
                        } else {
                            viewsToRemove.add(child)
                        }
                    }
                    viewsToRemove.forEach { l.removeView(it) }
                }
            }

            val actionBarMenuSubItemClass = findClass("org.telegram.ui.ActionBar.ActionBarMenuSubItem")
            val messagePrivateSeenView = findClass("org.telegram.ui.Components.MessagePrivateSeenView")
            val gapView = findClass("org.telegram.ui.ActionBar.ActionBarPopupWindow\$GapView")
            val allowedItems = listOf(getStringResource("Copy"), getStringResource("Delete"))
            popupLayout?.let { p ->
                val viewsToRemove = mutableListOf<View>()
                val l = getObjectField(p, "linearLayout") as LinearLayout
                val childCount = l.childCount
                for (i in 0 until childCount) {
                    val child = l.getChildAt(i)
                    if (messagePrivateSeenView.isInstance(child)) {
                        if (getIntField(child, "type") == SEEN_TYPE_SEEN) {
                            viewsToRemove.add(child)
                        }
                    } else if (actionBarMenuSubItemClass.isInstance(child)) {
                        val textView = getObjectField(child, "textView")
                        val text = callMethod(textView, "getText") as? String ?: continue
                        if (text !in allowedItems) {
                            viewsToRemove.add(child)
                        }
                    }
                }
                viewsToRemove.forEach { l.removeView(it) }
                if (gapView.isInstance(l.getChildAt(0))) {
                    l.removeViewAt(0)
                }
            }
        }

        findAndHook(
            "org.telegram.ui.ChatActivity",
            "createMenu",
            HookStage.BEFORE,
            filter = {
                Config.isHookEnabled("show_deleted_messages")
            },
        ) { param ->
            val p1 = param.arg<Any>(0)
            val chatMessageCellClass = findClass("org.telegram.ui.Cells.ChatMessageCell")
            if (!chatMessageCellClass.isInstance(p1)) return@findAndHook
            val chatMsgCellObj = ChatMessageCell(p1)

            val msgObj = chatMsgCellObj.getMessageObject()

            // val type = callMethod(o, "getMessageType", msg) as Int
            // if (type != 3) return@hook

            val mid = msgObj.getId()
            val did = msgObj.getDialogId()

            msgIsDeleted = false
            runBlocking {
                launch {
                    db.deletedMessageDao().get(mid, did)?.let {
                        msgIsDeleted = true
                    }
                }.join()
            }
        }

        findAndHook(
            "org.telegram.ui.Components.MessagePrivateSeenView",
            "request",
            HookStage.BEFORE,
            filter = {
                Config.isHookEnabled("show_deleted_messages")
            },
        ) { param ->
            val o = param.thisObject()
            if (!msgIsDeleted || (getIntField(o, "type") != SEEN_TYPE_SEEN)) return@findAndHook
            param.setResult(null)
        }
    }
}
