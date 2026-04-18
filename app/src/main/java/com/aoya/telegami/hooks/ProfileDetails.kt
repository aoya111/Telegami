package com.aoya.telegami.hooks

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.messenger.AndroidUtilities
import com.aoya.telegami.virt.messenger.ChatObject
import com.aoya.telegami.virt.messenger.LocaleController
import com.aoya.telegami.virt.messenger.UserObject
import com.aoya.telegami.virt.ui.ProfileActivity
import com.aoya.telegami.virt.ui.components.ItemOptions
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.i18n.TranslationManager as i18n
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object ProfileDetails : YukiBaseHooker() {
    const val PROFILE_ACTIVITY_CN = "org.telegram.ui.ProfileActivity"
    val profileActivityClass by lazyClass(resolver.get(PROFILE_ACTIVITY_CN))

    override fun onHook() {
        profileActivityClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(PROFILE_ACTIVITY_CN, "processOnClickOrPress")
            }.hook {
                before {
                    val o = ProfileActivity(instance)

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
                    resultFalse()
                }
            }
    }
}
