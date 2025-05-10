package com.aoya.televip.hooks

import com.aoya.televip.TeleVip
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setBooleanField
import com.aoya.televip.core.i18n.TranslationManager as i18n
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class AddChatNavigation :
    Hook(
        "add_chat_navigation",
        "Add chat navigation",
    ) {
    override fun init() {
        findClass(
            "org.telegram.ui.ChatActivity",
        ).hook(resolver.getMethod("org.telegram.ui.ChatActivity", "createView"), HookStage.AFTER) { param ->
            val o = param.thisObject()
            val headerItem = getObjectField(o, "headerItem")
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
                callMethod(
                    headerItem,
                    resolver.getMethod("org.telegram.ui.ActionBar.ActionBarMenuItem", "lazilyAddColoredGap"),
                )
                if (TeleVip.packageName !in pkgNames) {
                    callMethod(
                        headerItem,
                        resolver.getMethod("org.telegram.ui.ActionBar.ActionBarMenuItem", "lazilyAddSubItem"),
                        70,
                        getResource("msg_go_up", "drawable"),
                        i18n.get("ChatScrollToTop"),
                    )
                }
            }
        }

        val suffixMap =
            mapOf(
                "org.telegram.plus" to "14",
                "org.forkclient.messenger.beta" to "15",
                "org.forkgram.messenger" to "15",
            )

        val suffix = suffixMap[TeleVip.packageName] ?: "13"
        findClass(
            "org.telegram.ui.ChatActivity\$$suffix",
        ).hook(resolver.getMethod("org.telegram.ui.ChatActivity\$$suffix", "onItemClick"), HookStage.AFTER) { param ->
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
                setBooleanField(
                    chatActivity,
                    resolver.getMethod("org.telegram.ui.ChatActivity", "canShowPagedownButton"),
                    true,
                )
                callMethod(
                    chatActivity,
                    resolver.getMethod("org.telegram.ui.ChatActivity", "updatePagedownButtonVisibility"),
                    true,
                )
                setBooleanField(
                    chatActivity,
                    resolver.getMethod("org.telegram.ui.ChatActivity", "pagedownButtonShowedByScroll"),
                    true,
                )
            }
        }
    }
}
