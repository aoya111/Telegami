package com.aoya.telegami.hooks

import com.aoya.telegami.service.Config
import com.aoya.telegami.util.logd
import com.aoya.telegami.virt.tgnet.RequestDelegate
import com.aoya.telegami.virt.tgnet.TLRPC
import com.aoya.telegami.virt.tgnet.tl.TLAccount
import com.aoya.telegami.virt.ui.ProfileActivity
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.i18n.TranslationManager as i18n
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object Privacy : YukiBaseHooker() {
    const val CONNECTIONS_MANAGER_CN = "org.telegram.tgnet.ConnectionsManager"
    const val PROFILE_ACTIVITY_CN = "org.telegram.ui.ProfileActivity"

    val connectionsManagerClass by lazyClass(resolver.get(CONNECTIONS_MANAGER_CN))
    val profileActivityClass by lazyClass(resolver.get(PROFILE_ACTIVITY_CN))

    val tlMessagesSetEncTypingClass = resolver.get("org.telegram.tgnet.TLRPC\$TL_messages_setEncryptedTyping").toClass()
    val tlMessagesSetTypingClass = resolver.get("org.telegram.tgnet.TLRPC\$TL_messages_setTyping").toClass()
    val tlAccountUpdateStatusClass = resolver.get("org.telegram.tgnet.tl.TL_account\$updateStatus").toClass()

    val tlMessagesReadHistoryClass = resolver.get("org.telegram.tgnet.TLRPC\$TL_messages_readHistory").toClass()
    val tlMessagesReadEncHistoryClass = resolver.get("org.telegram.tgnet.TLRPC\$TL_messages_readEncryptedHistory").toClass()
    val tlMessagesReadDiscussionClass = resolver.get("org.telegram.tgnet.TLRPC\$TL_messages_readDiscussion").toClass()
    val tlMessagesReadMessageContentsClass = resolver.get("org.telegram.tgnet.TLRPC\$TL_messages_readMessageContents").toClass()
    val tlChannelsReadHistoryClass = resolver.get("org.telegram.tgnet.TLRPC\$TL_channels_readHistory").toClass()
    val tlChannelsReadMessageContentsClass = resolver.get("org.telegram.tgnet.TLRPC\$TL_channels_readMessageContents").toClass()

    override fun onHook() {
        connectionsManagerClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(CONNECTIONS_MANAGER_CN, "sendRequestInternal")
            }.hook {
                before {
                    val o = args[0] ?: return@before

                    if ((tlMessagesSetTypingClass.isInstance(o) || tlMessagesSetEncTypingClass.isInstance(o)) &&
                        Config.isFeatureEnabled("HideTyping")
                    ) {
                        logd("[PrivacyHook] should HideTyping")
                        resultNull()
                    }

                    if (tlAccountUpdateStatusClass.isInstance(o) && Config.isFeatureEnabled("HideOnlineStatus")) {
                        logd("[PrivacyHook] should HideOnlineStatus")
                        TLAccount.UpdateStatus(o).offline = true
                    }

                    if ((
                            (
                                tlMessagesReadHistoryClass.isInstance(o) || tlMessagesReadEncHistoryClass.isInstance(o) ||
                                    tlMessagesReadDiscussionClass.isInstance(o) ||
                                    tlMessagesReadMessageContentsClass.isInstance(o)
                            ) && Config.isFeatureEnabled("HideSeenPrivateChat")
                        ) ||
                        (
                            (
                                tlChannelsReadHistoryClass.isInstance(o) ||
                                    tlChannelsReadMessageContentsClass.isInstance(o)
                            ) && Config.isFeatureEnabled("HideSeenChannel")
                        )
                    ) {
                        logd("[PrivacyHook] should HideSeen")
                        val fakeRes = TLRPC.TLMessagesAffectedMessages()
                        fakeRes.pts = -1
                        fakeRes.ptsCount = 0
                        val onComplete = args[1]
                        onComplete?.let {
                            RequestDelegate(it).run(fakeRes.getNativeInstance(), null)
                        }
                        resultNull()
                    }
                }
            }

        if (Config.isFeatureEnabled("HideOnlineStatus")) {
            profileActivityClass
                .resolve()
                .firstMethod {
                    name = resolver.getMethod(PROFILE_ACTIVITY_CN, "updateProfileData")
                }.hook {
                    after {
                        val o = ProfileActivity(instance)

                        val clientUserId = o.getUserConfig().getClientUserId()
                        val userId = o.userId

                        if (clientUserId != 0L && userId != 0L && userId == clientUserId) {
                            o.onlineTextView.getOrNull(1)?.setText(i18n.get("ProfileStatusOffline"))
                        }
                    }
                }
        }
    }
}
