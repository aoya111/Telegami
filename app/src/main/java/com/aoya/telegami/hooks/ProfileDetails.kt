package com.aoya.telegami.hooks

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.hook
import com.aoya.telegami.virt.messenger.AndroidUtilities
import com.aoya.telegami.virt.messenger.ChatObject
import com.aoya.telegami.virt.messenger.UserObject
import com.aoya.telegami.virt.ui.ProfileActivity
import com.aoya.telegami.virt.ui.components.ItemOptions
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.getBooleanField
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.getStaticBooleanField
import de.robv.android.xposed.XposedHelpers.newInstance
import com.aoya.telegami.core.i18n.TranslationManager as i18n
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ProfileDetails :
    Hook(
        "profile_details",
        "Add extra fields and details to profiles",
    ) {
    override fun init() {
        findClass(
            "org.telegram.ui.ProfileActivity",
        ).hook(resolver.getMethod("org.telegram.ui.ProfileActivity", "editRow"), HookStage.BEFORE) { param ->
            val o = ProfileActivity(param.thisObject())

            val view = param.arg<Any>(0) as? View ?: return@hook

            if (!o.myProfile) return@hook

            if (param.arg<Int>(1) != o.usernameRow) return@hook

            param.setResult(true)
        }

        findClass(
            "org.telegram.ui.ProfileActivity",
        ).hook(resolver.getMethod("org.telegram.ui.ProfileActivity", "processOnClickOrPress"), HookStage.BEFORE) { param ->
            val prof = ProfileActivity(param.thisObject())

            val usernameRow = prof.usernameRow

            if (param.arg<Int>(0) != usernameRow) return@hook
            val view = param.arg<Any>(1) as? View ?: return@hook

            val chatId = prof.chatId
            val userId = prof.userId

            val contentView = prof.contentView as? ViewGroup ?: return@hook
            val resourcesProvider = prof.resourcesProvider

            val itemOptions = ItemOptions.makeOptions(contentView, resourcesProvider, view, false)
            itemOptions.setGravity(Gravity.LEFT)

            val msgCtrl = prof.getMessagesController()
            val (username, id, idLabel) =
                when {
                    userId != 0L -> {
                        val user = msgCtrl.getUser(userId) ?: return@hook
                        val username = UserObject.getPublicUsername(user) ?: return@hook
                        Triple(username, userId, i18n.get("ProfileCopyUserId"))
                    }

                    chatId != 0L -> {
                        val chat = msgCtrl.getChat(chatId) ?: return@hook
                        val topicId = prof.topicId
                        if (topicId == 0L && !ChatObject.isPublic(chat)) return@hook
                        val username = ChatObject.getPublicUsername(chat) ?: return@hook
                        Triple(username, chatId, i18n.get("ProfileCopyChatId"))
                    }

                    else -> {
                        return@hook
                    }
                }
            itemOptions
                .add(
                    getResource("msg_copy", "drawable"),
                    getStringResource("ProfileCopyUsername"),
                    Runnable { AndroidUtilities.addToClipboard(username) },
                ).add(
                    getResource("msg_copy", "drawable"),
                    idLabel,
                    Runnable { AndroidUtilities.addToClipboard(id.toString()) },
                )

            if (userId != 0L && prof.myProfile) {
                itemOptions
                    .add(
                        getResource("msg_edit", "drawable"),
                        getStringResource("ProfileUsernameEdit"),
                        Runnable {
                            prof.presentFragment(newInstance(findClass("org.telegram.ui.ChangeUsernameActivity")))
                        },
                    )
            }

            itemOptions.show()
        }
    }
}
