package com.aoya.televip.hooks

import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import com.aoya.televip.TeleVip
import com.aoya.televip.ui.AlertDialogBuilder
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getObjectField
import com.aoya.televip.core.i18n.TranslationManager as i18n
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class AddChatNavigation :
    Hook(
        "add_chat_navigation",
        "Add chat navigation",
    ) {
    override fun init() {
        var isshow = false

        try {
            if (!isshow) {
                findClass(
                    "org.telegram.ui.ChatActivity",
                ).hook(resolver.getMethod("org.telegram.ui.ChatActivity", "createView"), HookStage.AFTER) { param ->
                    val headerItem = getObjectField(param.thisObject(), "headerItem")
                    if (TeleVip.packageName != "xyz.nextalone.nagram") {
                        val pkgNames =
                            listOf(
                                "com.skyGram.bestt",
                                "uz.unnarsx.cherrygram",
                                "com.iMe.android",
                                "app.nicegram",
                                "org.telegram.plus",
                                "com.xplus.messenger",
                                "org.forkgram.messenger",
                                "org.forkclient.messenger.beta",
                            )
                        if (TeleVip.packageName !in pkgNames) {
                            callMethod(
                                headerItem,
                                resolver.getMethod("org.telegram.ui.ActionBar.ActionBarMenuItem", "lazilyAddSubItem"),
                                70,
                                getResource("msg_go_up", "drawable"),
                                i18n.get("go_to_first_msg"),
                            )
                        }
                        callMethod(
                            headerItem,
                            resolver.getMethod("org.telegram.ui.ActionBar.ActionBarMenuItem", "lazilyAddSubItem"),
                            71,
                            getResource("player_new_order", "drawable"),
                            i18n.get("go_to_msg"),
                        )
                    }
                    isshow = true
                }
            }

            val suffixMap =
                mapOf(
                    "com.iMe.android" to "19",
                    "org.telegram.plus" to "14",
                    "org.forkclient.messenger.beta" to "15",
                    "org.forkgram.messenger" to "15",
                )

            val suffix = suffixMap[TeleVip.packageName] ?: "13"
            findClass("org.telegram.ui.ChatActivity\$$suffix").hook("onItemClick", HookStage.AFTER) { param ->
                val id = param.arg<Int>(0)
                val chatActivity = getObjectField(param.thisObject(), "this$0")
                if (id == 70) {
                    callMethod(
                        chatActivity,
                        resolver.getMethod("org.telegram.ui.ChatActivity", "scrollToMessageId"),
                        1,
                        0,
                        true,
                        0,
                        true,
                        0,
                    )
                } else if (id == 71) {
                    val ctx = callMethod(chatActivity, resolver.getMethod("org.telegram.ui.ChatActivity", "getContext")) as Context
                    val getResourceProvider =
                        callMethod(chatActivity, resolver.getMethod("org.telegram.ui.ChatActivity", "getResourceProvider"))

                    val editText = EditText(ctx)
                    if (!isDark) {
                        editText.setTextColor((-0x1000000).toInt())
                        editText.setHintTextColor((-0xBDBDBE).toInt())
                    } else {
                        editText.setTextColor((-0x1).toInt())
                        editText.setHintTextColor((-0x424243).toInt())
                    }
                    editText.textSize = 18f
                    editText.setPadding(20, 20, 20, 20)

                    val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    params.setMargins(20, 20, 20, 20)
                    editText.setLayoutParams(params)

                    val layout = LinearLayout(ctx)
                    layout.setOrientation(LinearLayout.VERTICAL)
                    layout.setPadding(30, 30, 30, 30)
                    layout.addView(editText)

                    AlertDialogBuilder(ctx, getResourceProvider)
                        .setTitle(i18n.get("input_msg_id"))
                        .setView(layout)
                        .setPositiveButton(i18n.get("done")) { dialog ->
                            try {
                                val inputText = editText.text.toString().trim()

                                if (inputText.isNotEmpty()) {
                                    callMethod(
                                        chatActivity,
                                        resolver.getMethod("org.telegram.ui.ChatActivity", "scrollToMessageId"),
                                        inputText.toInt(),
                                        0,
                                        true,
                                        0,
                                        true,
                                        0,
                                    )

                                    dialog.dismiss()
                                }
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
                }
            }
        } catch (e: ClassNotFoundException) {
            XposedBridge.log("Class not found: ${e.message}")
            e.printStackTrace()
        }
    }
}
