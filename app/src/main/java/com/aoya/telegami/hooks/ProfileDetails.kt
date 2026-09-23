package com.aoya.telegami.hooks

import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aoya.telegami.Telegami
import com.aoya.telegami.util.findMethod
import com.aoya.telegami.virt.messenger.AndroidUtilities
import com.aoya.telegami.virt.messenger.ChatObject
import com.aoya.telegami.virt.messenger.LocaleController
import com.aoya.telegami.virt.messenger.UserObject
import com.aoya.telegami.virt.ui.ProfileActivity
import com.aoya.telegami.virt.ui.components.ItemOptions
import io.github.libxposed.api.XposedInterface
import com.aoya.telegami.core.i18n.TranslationManager as i18n
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object ProfileDetails {
    const val PROFILE_ACTIVITY_CN = "org.telegram.ui.ProfileActivity"

    fun install(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        val method = classLoader.findMethod(PROFILE_ACTIVITY_CN, "processOnClickOrPress")
        xposed.hook(method).intercept { chain ->
            val args = chain.args.toMutableList()
            var result: Any? = null
            var hasResult = false
            try {
                run before@{
                    val o = ProfileActivity(chain.thisObject!!)

                    val rowIdx = args[0] as Int
                    if (rowIdx != o.usernameRow) return@before

                    val chatId = o.chatId
                    val userId = o.userId

                    val msgCtrl = o.getMessagesController()
                    val (username, id, idLabel) =
                        when {
                            userId != 0L -> {
                                val user = msgCtrl?.getUser(userId) ?: return@before
                                val username = UserObject.getPublicUsername(user) ?: return@before
                                Triple(username, userId, i18n.get("ProfileCopyUserId"))
                            }

                            chatId != 0L -> {
                                val chat = msgCtrl?.getChat(chatId) ?: return@before
                                val topicId = o.topicId
                                if (topicId == 0L && !ChatObject.isPublic(chat)) return@before
                                val username = ChatObject.getPublicUsername(chat) ?: return@before
                                Triple(username, chatId, i18n.get("ProfileCopyChatId"))
                            }

                            else -> {
                                return@before
                            }
                        }

                    val contentView = o.contentView as? ViewGroup ?: return@before
                    val resourcesProvider = o.resourcesProvider
                    val view = args[1] as View
                    val msgCopyId = Telegami.getResource("msg_copy", "drawable") ?: 0

                    val itemOptions = ItemOptions.makeOptions(contentView, resourcesProvider, view, false)
                    itemOptions.setGravity(Gravity.LEFT)
                    itemOptions
                        .add(
                            msgCopyId,
                            Telegami
                                .getResource(
                                    "ProfileCopyUsername",
                                    "string",
                                )?.takeIf { it != 0 }
                                ?.let { LocaleController.getString(it) }
                                ?: "Copy",
                            Runnable {
                                AndroidUtilities.addToClipboard(username)
                                val msg = i18n.get("CopiedToClipboardHint").replace("{item}", "username")
                                Telegami.showToast(Toast.LENGTH_SHORT, msg)
                            },
                        ).add(
                            msgCopyId,
                            idLabel,
                            Runnable {
                                AndroidUtilities.addToClipboard(id.toString())
                                val msg = i18n.get("CopiedToClipboardHint").replace("{item}", "ID")
                                Telegami.showToast(Toast.LENGTH_SHORT, msg)
                            },
                        )
                    itemOptions.show()
                    result = false
                    hasResult = true
                }
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed before ${method.name}", throwable)
            }
            if (!hasResult) {
                result = chain.proceed(args.toTypedArray())
                hasResult = true
            }
            result
        }
    }
}
