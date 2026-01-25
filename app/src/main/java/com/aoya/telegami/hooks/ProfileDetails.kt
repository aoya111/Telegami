package com.aoya.telegami.hooks

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aoya.telegami.Telegami
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.messenger.AndroidUtilities
import com.aoya.telegami.virt.messenger.ChatObject
import com.aoya.telegami.virt.messenger.UserObject
import com.aoya.telegami.virt.ui.ChangeUsernameActivity
import com.aoya.telegami.virt.ui.ProfileActivity
import com.aoya.telegami.virt.ui.components.ItemOptions
import com.aoya.telegami.core.i18n.TranslationManager as i18n

class ProfileDetails :
    Hook(
        "profile_details",
        "Add extra fields and details to profiles",
    ) {
    override fun init() {
        findAndHook("org.telegram.ui.ProfileActivity", "editRow", HookStage.BEFORE, filter = { true }) { param ->
            val o = ProfileActivity(param.thisObject())
            val view = param.arg<Any>(0) as? View ?: return@findAndHook
            if (!o.myProfile) return@findAndHook
            if (param.arg<Int>(1) != o.usernameRow) return@findAndHook

            param.setResult(true)
        }

        findAndHook("org.telegram.ui.ProfileActivity", "processOnClickOrPress", HookStage.BEFORE, filter = { true }) { param ->
            val prof = ProfileActivity(param.thisObject())

            val usernameRow = prof.usernameRow

            if (param.arg<Int>(0) != usernameRow) return@findAndHook
            val view = param.arg<Any>(1) as? View ?: return@findAndHook

            val chatId = prof.chatId
            val userId = prof.userId

            val contentView = prof.contentView as? ViewGroup ?: return@findAndHook
            val resourcesProvider = prof.resourcesProvider

            val itemOptions = ItemOptions.makeOptions(contentView, resourcesProvider, view, false)
            itemOptions.setGravity(Gravity.LEFT)

            val msgCtrl = prof.getMessagesController()
            val (username, id, idLabel) =
                when {
                    userId != 0L -> {
                        val user = msgCtrl.getUser(userId) ?: return@findAndHook
                        val username = UserObject.getPublicUsername(user) ?: return@findAndHook
                        Triple(username, userId, i18n.get("ProfileCopyUserId"))
                    }

                    chatId != 0L -> {
                        val chat = msgCtrl.getChat(chatId) ?: return@findAndHook
                        val topicId = prof.topicId
                        if (topicId == 0L && !ChatObject.isPublic(chat)) return@findAndHook
                        val username = ChatObject.getPublicUsername(chat) ?: return@findAndHook
                        Triple(username, chatId, i18n.get("ProfileCopyChatId"))
                    }

                    else -> {
                        return@findAndHook
                    }
                }
            itemOptions
                .add(
                    getResource("msg_copy", "drawable"),
                    getStringResource("ProfileCopyUsername"),
                    Runnable {
                        AndroidUtilities.addToClipboard(username)
                        val msg = i18n.get("CopiedToClipboardHint").replace("{item}", "username")
                        Telegami.showToast(Toast.LENGTH_SHORT, msg)
                    },
                ).add(
                    getResource("msg_copy", "drawable"),
                    idLabel,
                    Runnable {
                        AndroidUtilities.addToClipboard(id.toString())
                        val msg = i18n.get("CopiedToClipboardHint").replace("{item}", "ID")
                        Telegami.showToast(Toast.LENGTH_SHORT, msg)
                    },
                )

            if (userId != 0L && prof.myProfile) {
                itemOptions
                    .add(
                        getResource("msg_edit", "drawable"),
                        getStringResource("ProfileUsernameEdit"),
                        Runnable {
                            prof.presentFragment(ChangeUsernameActivity.newInstance().getNativeInstance())
                        },
                    )
            }

            itemOptions.show()
        }
    }
}
