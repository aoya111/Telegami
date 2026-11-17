package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.service.Config
import com.aoya.telegami.util.logd
import com.aoya.telegami.virt.tgnet.RequestDelegate
import com.aoya.telegami.virt.tgnet.TLRPC
import com.aoya.telegami.virt.tgnet.tl.TLAccount
import com.aoya.telegami.virt.ui.ProfileActivity
import com.aoya.telegami.util.findMethod
import com.aoya.telegami.core.i18n.TranslationManager as i18n
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver
import android.util.Log
import io.github.libxposed.api.XposedInterface

object Privacy {
    const val CONNECTIONS_MANAGER_CN = "org.telegram.tgnet.ConnectionsManager"
    const val PROFILE_ACTIVITY_CN = "org.telegram.ui.ProfileActivity"

    fun install(xposed: XposedInterface, classLoader: ClassLoader) {
        val classOf: (String) -> Class<*> = { classLoader.loadClass(resolver.get(it)) }
        val tlMessagesSetEncTypingClass = classOf("org.telegram.tgnet.TLRPC\$TL_messages_setEncryptedTyping")
        val tlMessagesSetTypingClass = classOf("org.telegram.tgnet.TLRPC\$TL_messages_setTyping")
        val tlAccountUpdateStatusClass = classOf("org.telegram.tgnet.tl.TL_account\$updateStatus")
        val tlMessagesReadHistoryClass = classOf("org.telegram.tgnet.TLRPC\$TL_messages_readHistory")
        val tlMessagesReadEncHistoryClass = classOf("org.telegram.tgnet.TLRPC\$TL_messages_readEncryptedHistory")
        val tlMessagesReadDiscussionClass = classOf("org.telegram.tgnet.TLRPC\$TL_messages_readDiscussion")
        val tlMessagesReadMessageContentsClass = classOf("org.telegram.tgnet.TLRPC\$TL_messages_readMessageContents")
        val tlChannelsReadHistoryClass = classOf("org.telegram.tgnet.TLRPC\$TL_channels_readHistory")
        val tlChannelsReadMessageContentsClass = classOf("org.telegram.tgnet.TLRPC\$TL_channels_readMessageContents")
        val sendRequestInternal = classLoader.findMethod(CONNECTIONS_MANAGER_CN, "sendRequestInternal")
        xposed.hook(sendRequestInternal).intercept { chain ->
            val args = chain.args.toMutableList()
            var result: Any? = null
            var hasResult = false
            try {
                run before@ {
                    val o = args[0] ?: return@before

                    if ((tlMessagesSetTypingClass.isInstance(o) || tlMessagesSetEncTypingClass.isInstance(o)) &&
                        Config.isFeatureEnabled("HideTyping")
                    ) {
                        logd("[PrivacyHook] should HideTyping")
                        result = null
                        hasResult = true
                    }

                    if (tlAccountUpdateStatusClass.isInstance(o) && Config.isFeatureEnabled("HideOnlineStatus")) {
                        logd("[PrivacyHook] should HideOnlineStatus")
                        TLAccount.UpdateStatus(o).offline = true
                    }

                    val isNeko = Telegami.packageName == "tw.nekomimi.nekogram"
                    val channelRead =
                        if (isNeko) {
                            (tlChannelsReadHistoryClass.isInstance(o) && byteField(o) == 2) ||
                                (tlChannelsReadMessageContentsClass.isInstance(o) && byteField(o) == 1)
                        } else {
                            tlChannelsReadHistoryClass.isInstance(o) ||
                                tlChannelsReadMessageContentsClass.isInstance(o)
                        }
                    val encryptedRead =
                        isNeko &&
                            tlMessagesReadEncHistoryClass.isInstance(o) &&
                            byteField(o) == 2
                    val privateRead =
                        if (isNeko) {
                            (tlMessagesReadHistoryClass.isInstance(o) && byteField(o) == 5) ||
                                (tlMessagesReadMessageContentsClass.isInstance(o) && byteField(o) == 9) ||
                                encryptedRead
                        } else {
                            tlMessagesReadHistoryClass.isInstance(o) ||
                                tlMessagesReadDiscussionClass.isInstance(o) ||
                                tlMessagesReadMessageContentsClass.isInstance(o)
                        }

                    if ((privateRead && Config.isFeatureEnabled("HideSeenPrivateChat")) ||
                        (channelRead && Config.isFeatureEnabled("HideSeenChannel"))
                    ) {
                        logd("[PrivacyHook] should HideSeen")
                        val onComplete = args[1]
                        // 13.2.2 channel read-history and encrypted-history
                        // responses are Bool. Returning affectedMessages here
                        // makes the host callback cast it to messages_Messages
                        // and crash.
                        val boolResponse = isNeko && (encryptedRead || channelRead)
                        if (onComplete != null && boolResponse) {
                            val boolTrue = TLRPC.TLBoolTrue.allocate()
                            if (boolTrue != null) {
                                RequestDelegate(onComplete).run(boolTrue.getNativeInstance(), null)
                                result = null
                                hasResult = true
                            } else {
                                logd("[PrivacyHook] fake bool response unavailable")
                            }
                        } else if (onComplete != null && !boolResponse) {
                            val fakeRes = TLRPC.TLMessagesAffectedMessages.allocate()
                            // Private read responses process pts; a fake -1
                            // corrupts the update state, so use the real pts.
                            val pts = if (isNeko) currentPts(classLoader) else -1
                            if (fakeRes != null && (!isNeko || pts >= 0)) {
                                fakeRes.pts = pts
                                fakeRes.ptsCount = 0
                                RequestDelegate(onComplete).run(fakeRes.getNativeInstance(), null)
                                result = null
                                hasResult = true
                            } else {
                                logd("[PrivacyHook] fake read response unavailable")
                            }
                        } else {
                            logd("[PrivacyHook] fake read response unavailable")
                        }
                    }
                }
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed before ${sendRequestInternal.name}", throwable)
            }
            if (!hasResult) {
                result = chain.proceed(args.toTypedArray())
                hasResult = true
            }
            result
        }

        if (Config.isFeatureEnabled("HideOnlineStatus")) {
            val updateProfileData = classLoader.findMethod(PROFILE_ACTIVITY_CN, "updateProfileData")
            xposed.hook(updateProfileData).intercept { chain ->
                val args = chain.args.toMutableList()
                val result = chain.proceed(args.toTypedArray())
                try {
                    val o = ProfileActivity(chain.thisObject!!)
                    if (isSelfProfile(o)) {
                        o.setStatusText(i18n.get("ProfileStatusOffline"))
                    }
                } catch (throwable: Throwable) {
                    Log.e("Telegami", "Hook callback failed after ${updateProfileData.name}", throwable)
                }
                result
            }

            // Some clients re-render the status without calling updateProfileData
            // again (for example when the online state changes). Re-apply the
            // offline label after the renderer runs so it cannot stick.
            runCatching {
                val updateOnlineText = classLoader.findMethod(PROFILE_ACTIVITY_CN, "updateOnlineText", 0)
                xposed.hook(updateOnlineText).intercept { chain ->
                    val result = chain.proceed(chain.args.toTypedArray())
                    try {
                        val o = ProfileActivity(chain.thisObject!!)
                        if (isSelfProfile(o)) {
                            o.setStatusText(i18n.get("ProfileStatusOffline"))
                        }
                    } catch (throwable: Throwable) {
                        Log.e("Telegami", "Hook callback failed after ${updateOnlineText.name}", throwable)
                    }
                    result
                }
            }.onFailure {
                Log.w("Telegami", "HideOnlineStatus updateOnlineText unavailable: ${it.message}")
            }
        }
    }

    private fun messagesController(classLoader: ClassLoader): Any? =
        runCatching {
            val userConfigClass = classLoader.loadClass(resolver.get("org.telegram.messenger.UserConfig"))
            val accountField =
                userConfigClass
                    .getDeclaredField(
                        resolver.getField("org.telegram.messenger.UserConfig", "selectedAccount"),
                    ).apply { isAccessible = true }
            val account = accountField.getInt(null)

            val controllerClass = classLoader.loadClass(resolver.get("org.telegram.messenger.MessagesController"))
            val getInstance =
                controllerClass
                    .getDeclaredMethod(
                        resolver.getMethod("org.telegram.messenger.MessagesController", "getInstance"),
                        Int::class.javaPrimitiveType,
                    ).apply { isAccessible = true }
            getInstance.invoke(null, account)
        }.getOrNull()

    private fun messagesStorage(classLoader: ClassLoader): Any? {
        val controller = messagesController(classLoader) ?: return null
        return runCatching {
            controller.javaClass.getMethod("getMessagesStorage").invoke(controller)
        }.getOrNull()
    }

    private fun currentPts(classLoader: ClassLoader): Int {
        val storage = messagesStorage(classLoader) ?: return -1
        return runCatching {
            storage.javaClass
                .getField(
                    resolver.getField("org.telegram.messenger.MessagesStorage", "lastPtsValue"),
                ).apply { isAccessible = true }
                .getInt(storage)
        }.getOrDefault(-1)
    }

    private fun findField(
        target: Any,
        name: String,
    ): Any? {
        var clazz: Class<*>? = target.javaClass
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name).apply { isAccessible = true }.get(target)
            } catch (_: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        return null
    }

    private fun byteField(request: Any): Int? = (findField(request, "a") as? Byte)?.toInt()

    private fun isSelfProfile(o: ProfileActivity): Boolean = o.myProfile
}
