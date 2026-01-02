package com.aoya.televip.hooks

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.aoya.televip.ui.components.ItemOptions
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.getBooleanField
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.getStaticBooleanField
import de.robv.android.xposed.XposedHelpers.newInstance
import com.aoya.televip.core.i18n.TranslationManager as i18n
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class ProfileDetails :
    Hook(
        "profile_details",
        "Add extra fields and details to profiles",
    ) {
    override fun init() {
        findClass(
            "org.telegram.ui.ProfileActivity",
        ).hook(resolver.getMethod("org.telegram.ui.ProfileActivity", "editRow"), HookStage.BEFORE) { param ->
            val o = param.thisObject()

            val view = param.arg<Any>(0) as? View
            if (view == null) return@hook

            if (!getBooleanField(o, "myProfile")) return@hook

            val usernameRow = getIntField(o, "usernameRow")
            if (param.arg<Int>(1) != usernameRow) return@hook

            param.setResult(true)
        }

        findClass(
            "org.telegram.ui.ProfileActivity",
        ).hook(resolver.getMethod("org.telegram.ui.ProfileActivity", "processOnClickOrPress"), HookStage.BEFORE) { param ->
            val o = param.thisObject()

            val usernameRow = getIntField(o, "usernameRow")

            if (param.arg<Int>(0) != usernameRow) return@hook
            val view = param.arg<Any>(1) as? View
            if (view == null) return@hook

            val chatId = getLongField(o, "chatId")
            val userId = getLongField(o, "userId")

            var username: String

            val contentView = getObjectField(o, "contentView") as? ViewGroup
            val resourcesProvider = getObjectField(o, "resourcesProvider")
            if (contentView == null) return@hook
            val itemOptions = ItemOptions.makeOptions(contentView, resourcesProvider, view, false)
            itemOptions.setGravity(Gravity.LEFT)

            val msgCtrl = callMethod(o, resolver.getMethod("org.telegram.ui.ActionBar.BaseFragment", "getMessagesController"))
            if (userId != 0L) {
                val user = callMethod(msgCtrl, resolver.getMethod("org.telegram.messenger.MessagesController", "getUser"), userId)
                val username1 =
                    callStaticMethod(
                        findClass(resolver.get("org.telegram.messenger.UserObject")),
                        resolver.getMethod("org.telegram.messenger.UserObject", "getPublicUsername"),
                        user,
                    )
                if (user == null || username1 == null) return@hook
                username = username1 as String
                itemOptions
                    .add(
                        getResource("msg_copy", "drawable"),
                        getStringResource("ProfileCopyUsername"),
                        Runnable {
                            callStaticMethod(
                                findClass(resolver.get("org.telegram.messenger.AndroidUtilities")),
                                resolver.getMethod("org.telegram.messenger.AndroidUtilities", "addToClipboard"),
                                username,
                            )
                        },
                    ).add(
                        getResource("msg_copy", "drawable"),
                        i18n.get("ProfileCopyUserId"),
                        Runnable {
                            callStaticMethod(
                                findClass(resolver.get("org.telegram.messenger.AndroidUtilities")),
                                resolver.getMethod("org.telegram.messenger.AndroidUtilities", "addToClipboard"),
                                userId.toString(),
                            )
                        },
                    )
            } else if (chatId != 0L) {
                val chat = callMethod(msgCtrl, resolver.getMethod("org.telegram.messenger.MessagesController", "getChat"), chatId)
                val topicId = getLongField(o, "topicId")
                val chatObjClass = findClass("org.telegram.messenger.ChatObject")
                if (chat == null || topicId == 0L && !getStaticBooleanField(chatObjClass, "isPublic")) return@hook
                username = callStaticMethod(chatObjClass, "getPublicUsername", chat) as String
                itemOptions
                    .add(
                        getResource("msg_copy", "drawable"),
                        getStringResource("ProfileCopyUsername"),
                        Runnable {
                            callStaticMethod(
                                findClass(resolver.get("org.telegram.messenger.AndroidUtilities")),
                                resolver.getMethod("org.telegram.messenger.AndroidUtilities", "addToClipboard"),
                                username,
                            )
                        },
                    ).add(
                        getResource("msg_copy", "drawable"),
                        i18n.get("ProfileCopyChatId"),
                        Runnable {
                            callStaticMethod(
                                findClass(resolver.get("org.telegram.messenger.AndroidUtilities")),
                                resolver.getMethod("org.telegram.messenger.AndroidUtilities", "addToClipboard"),
                                chatId.toString(),
                            )
                        },
                    )
            } else {
                return@hook
            }

            if (userId != 0L && getBooleanField(o, "myProfile")) {
                itemOptions
                    .add(
                        getResource("msg_edit", "drawable"),
                        getStringResource("ProfileUsernameEdit"),
                        Runnable {
                            callMethod(
                                o,
                                "presentFragment",
                                newInstance(findClass("org.telegram.ui.ChangeUsernameActivity")),
                            )
                        },
                    )
            }

            itemOptions.show()
        }
    }
}
