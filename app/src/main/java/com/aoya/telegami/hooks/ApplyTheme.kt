package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.service.Config
import com.aoya.telegami.service.UserConfig
import com.aoya.telegami.virt.tgnet.TLRPC
import com.aoya.telegami.virt.ui.PeerColorActivity
import com.aoya.telegami.util.findMethod
import com.aoya.telegami.util.logw
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver
import io.github.libxposed.api.XposedInterface
import android.util.Log

object ApplyTheme {
    const val PEER_COLOR_ACTIVITY_CN = "org.telegram.ui.PeerColorActivity"
    const val USER_CONFIG_CN = "org.telegram.messenger.UserConfig"
    fun install(xposed: XposedInterface, classLoader: ClassLoader) {
        if (Telegami.packageName in listOf("uz.unnarsx.cherrygram", "xyz.nextalone.nagram")) return
        if (!Config.isFeatureEnabled("UnlockChannelFeatures")) return
        val applyMethod =
            runCatching { classLoader.findMethod(PEER_COLOR_ACTIVITY_CN, "apply") }.getOrNull()
        if (applyMethod == null) {
            logw("ApplyTheme skipped: PeerColorActivity.apply is not mapped")
            return
        }
        xposed.hook(applyMethod).intercept { chain ->
            val args = chain.args.toMutableList()
            val result = chain.proceed(args.toTypedArray())
            try {
                    val o = chain.thisObject?.let { PeerColorActivity(it) } ?: return@intercept result

                    o.profilePage.selectedColor
                        .takeIf { it != 0 }
                        ?.let(UserConfig::setProfileColor)
                    o.profilePage.selectedEmoji
                        .takeIf { it != 0L }
                        ?.let(UserConfig::setProfileEmoji)
                    o.namePage.selectedColor
                        .takeIf { it != 0 }
                        ?.let(UserConfig::setNameColor)
                    o.namePage.selectedEmoji
                        .takeIf { it != 0L }
                        ?.let(UserConfig::setNameEmoji)
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed after ${applyMethod.name}", throwable)
            }
            result
        }
        val currentUserMethod =
            runCatching { classLoader.findMethod(USER_CONFIG_CN, "getCurrentUser") }.getOrNull()
        if (currentUserMethod == null) {
            logw("ApplyTheme skipped current-user hook: getCurrentUser is not mapped")
            return
        }
        xposed.hook(currentUserMethod).intercept { chain ->
            val args = chain.args.toMutableList()
            val result = chain.proceed(args.toTypedArray())
            try {
                    val user = result?.let { TLRPC.User(it) } ?: return@intercept result

                    var profileColor = user.profileColor
                    val storedProfileColor = UserConfig.getProfileColor()
                    val storedProfileEmoji = UserConfig.getProfileEmoji()
                    if (profileColor == null && (storedProfileColor != null || storedProfileEmoji != null)) {
                        profileColor = TLRPC.TLPeerColor.allocate()
                        if (profileColor != null) {
                            user.profileColor = profileColor
                        }
                    }
                    if (profileColor != null) {
                        storedProfileColor?.let {
                            profileColor.color = it
                            profileColor.flags = profileColor.flags or 1
                            user.flags2 = user.flags2 or 512
                        }
                        storedProfileEmoji?.let {
                            profileColor.backgroundEmojiId = it
                            profileColor.flags = profileColor.flags or 2
                            user.flags2 = user.flags2 or 512
                        }
                    }

                    var color = user.color
                    val storedNameColor = UserConfig.getNameColor()
                    val storedNameEmoji = UserConfig.getNameEmoji()
                    if (color == null && (storedNameColor != null || storedNameEmoji != null)) {
                        color = TLRPC.TLPeerColor.allocate()
                        if (color != null) {
                            user.color = color
                        }
                    }
                    if (color != null) {
                        val color2 = user.id % 7
                        color.color = color2.toInt()
                        storedNameColor?.let {
                            color.color = it
                            color.flags = color.flags or 1
                            user.flags2 = user.flags2 or 256
                        }
                        storedNameEmoji?.let {
                            color.backgroundEmojiId = it
                            color.flags = color.flags or 2
                            user.flags2 = user.flags2 or 256
                        }
                    }

                    if (profileColor != null) user.profileColor = profileColor
                    if (color != null) user.color = color
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed after ${currentUserMethod.name}", throwable)
            }
            result
        }
    }
}
