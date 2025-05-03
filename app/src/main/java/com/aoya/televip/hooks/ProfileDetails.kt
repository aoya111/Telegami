package com.aoya.televip.hooks

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.Toast
import com.aoya.televip.TeleVip
import com.aoya.televip.core.Config
import com.aoya.televip.ui.AlertDialogBuilder
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.getObjectField
import com.aoya.televip.core.i18n.TranslationManager as i18n
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class ProfileDetails :
    Hook(
        "Profile details",
        "Add extra fields and details to profiles",
    ) {
    override fun init() {
        val ctx = TeleVip.context
        var activeContactId = 0L
        findClass(
            "org.telegram.ui.ProfileActivity",
        ).hook(resolver.getMethod("org.telegram.ui.ProfileActivity", "createActionBarMenu"), HookStage.AFTER) { param ->
            val o = param.thisObject()
            try {
                val msgCtrl = callMethod(o, resolver.getMethod("org.telegram.ui.ActionBar.BaseFragment", "getMessagesController"))

                val chatId = getLongField(o, "chatId")
                val userId = getLongField(o, "userId")
                val otherItem = getObjectField(o, "otherItem") ?: return@hook

                val chat = callMethod(msgCtrl, resolver.getMethod("org.telegram.messenger.MessagesController", "getChat"), chatId)
                val user = callMethod(msgCtrl, resolver.getMethod("org.telegram.messenger.MessagesController", "getUser"), userId)

                val userMenu = getResource("msg_filled_menu_users", "drawable")
                val addSubItem: (Int, String) -> Any? = { itemId, label ->
                    callMethod(
                        otherItem,
                        resolver.getMethod("org.telegram.ui.ActionBar.ActionBarMenuItem", "addSubItem"),
                        itemId,
                        userMenu,
                        label,
                    )
                }

                if (chat != null) {
                    activeContactId = chatId
                    addSubItem(45, chatId.toString())
                } else if (user != null) {
                    activeContactId = userId
                    addSubItem(45, userId.toString())
                    if (Config.getContactNewName(userId).isEmpty()) {
                        addSubItem(46, i18n.get("change_name"))
                    } else {
                        addSubItem(47, i18n.get("delete_name"))
                    }
                }
            } catch (e: Exception) {
                XposedBridge.log("Error while handling createActionBarMenu: ${e.message}")
            }
        }

        findClass(
            "org.telegram.ui.ProfileActivity\$6",
        ).hook(resolver.getMethod("org.telegram.ui.ProfileActivity\$6", "onItemClick"), HookStage.AFTER) { param ->
            val itemId = param.arg<Int>(0)
            if (itemId == 45) {
                (
                    ctx.getSystemService(
                        Context.CLIPBOARD_SERVICE,
                    ) as ClipboardManager
                ).setPrimaryClip(ClipData.newPlainText("clipboard", activeContactId.toString()))
                TeleVip.showToast(Toast.LENGTH_SHORT, activeContactId.toString())
            } else if (itemId == 46) {
                val editText = EditText(ctx)
                if (!isDark) {
                    editText.setTextColor(0xFF000000.toInt())
                    editText.setHintTextColor(0xFF424242.toInt())
                } else {
                    editText.setTextColor(0xFFFFFFFF.toInt())
                    editText.setHintTextColor(0xFFBDBDBD.toInt())
                }
                editText.hint = i18n.get("change_name")
                editText.textSize = 18f
                editText.setPadding(20, 20, 20, 20)

                val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                params.setMargins(20, 20, 20, 20)
                editText.setLayoutParams(params)

                val layout = LinearLayout(ctx)
                layout.setOrientation(LinearLayout.VERTICAL)
                layout.setPadding(30, 30, 30, 30)
                layout.addView(editText)

                val appCtx =
                    callMethod(
                        getObjectField(param.thisObject(), "this$0"),
                        resolver.getMethod("org.telegram.ui.ProfileActivity", "getParentActivity"),
                    )
                        as Context
                AlertDialogBuilder(appCtx)
                    .setTitle(i18n.get("change_name"))
                    .setView(layout)
                    .setPositiveButton(i18n.get("change")) { dialog ->
                        try {
                            val inputText = editText.text.toString().trim()
                            if (inputText.isNotEmpty()) {
                                val tm = i18n.get("change_to")
                                TeleVip.showToast(Toast.LENGTH_SHORT, "$tm: $inputText")
                                Config.setContactNewName(activeContactId, inputText)
                            }
                            dialog.dismiss()
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }.setNegativeButton(i18n.get("cancel")) { dialog ->
                        try {
                            dialog.dismiss()
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }.show()
            } else if (itemId == 47) {
                Config.setContactNewName(activeContactId, "")
                TeleVip.showToast(Toast.LENGTH_SHORT, i18n.get("deleted"))
            }
        }

        // TODO: make this work with Nekogram
        findClass(
            "org.telegram.ui.ProfileActivity",
        ).hook(resolver.getMethod("org.telegram.ui.ProfileActivity", "createView"), HookStage.AFTER) { param ->
            val o = param.thisObject()
            val nameTextView = getObjectField(o, "nameTextView") as Array<*>
            val simpleTextView1 = nameTextView.get(1) ?: return@hook
            XposedBridge.log("nameTextView: $simpleTextView1")
            callMethod(
                simpleTextView1,
                "setOnClickListener",
                View.OnClickListener { _ ->
                    try {
                        val userId = getLongField(o, "userId")
                        val msgCtrl =
                            callMethod(o, resolver.getMethod("org.telegram.ui.ActionBar.BaseFragment", "getMessagesController"))
                        val user = callMethod(msgCtrl, resolver.getMethod("org.telegram.messenger.MessagesController", "getUser"), userId)

                        if (user != null) {
                            val userName = getObjectField(user, resolver.getField("org.telegram.tgnet.TLRPC.User", "username")) as String
                            (
                                ctx.getSystemService(
                                    Context.CLIPBOARD_SERVICE,
                                ) as ClipboardManager
                            ).setPrimaryClip(ClipData.newPlainText("clipboard", userName))
                            TeleVip.showToast(Toast.LENGTH_SHORT, i18n.get("copied_to_clipboard").replace("{item}", userName))
                        }
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        XposedBridge.log("Error occurred in onClick: ${t.message}")
                    }
                },
            )
        }
    }
}
