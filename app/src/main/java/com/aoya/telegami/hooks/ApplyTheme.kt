package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.service.Config
import com.aoya.telegami.service.UserConfig
import com.aoya.telegami.virt.tgnet.TLRPC
import com.aoya.telegami.virt.ui.PeerColorActivity
import com.aoya.telegami.util.findMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver
import io.github.libxposed.api.XposedInterface
import android.util.Log

object ApplyTheme {
    const val PEER_COLOR_ACTIVITY_CN = "org.telegram.ui.PeerColorActivity"
    const val USER_CONFIG_CN = "org.telegram.messenger.UserConfig"
    fun install(xposed: XposedInterface, classLoader: ClassLoader) {
        if (Telegami.packageName in listOf("uz.unnarsx.cherrygram", "xyz.nextalone.nagram")) return
        if (!Config.isFeatureEnabled("UnlockChannelFeatures")) return
        val applyMethod = classLoader.findMethod(PEER_COLOR_ACTIVITY_CN, "apply")
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
        val currentUserMethod = classLoader.findMethod(USER_CONFIG_CN, "getCurrentUser")
        xposed.hook(currentUserMethod).intercept { chain ->
            val args = chain.args.toMutableList()
            val result = chain.proceed(args.toTypedArray())
            try {
                    val user = result?.let { TLRPC.User(it) } ?: return@intercept result

                    val profileColor = user.profileColor ?: TLRPC.TLPeerColor()
                    UserConfig.getProfileColor()?.let {
                        profileColor.color = it
                        profileColor.flags = profileColor.flags or 1
                        user.flags2 = user.flags2 or 512
                    }
                    UserConfig.getProfileEmoji()?.let {
                        profileColor.backgroundEmojiId = it
                        profileColor.flags = profileColor.flags or 2
                        user.flags2 = user.flags2 or 512
                    }

                    val color = user.color ?: TLRPC.TLPeerColor()
                    val color2 = user.id % 7
                    color.color = color2.toInt()
                    UserConfig.getNameColor()?.let {
                        color.color = it
                        color.flags = color.flags or 1
                        user.flags2 = user.flags2 or 256
                    }
                    UserConfig.getNameEmoji()?.let {
                        color.backgroundEmojiId = it
                        color.flags = color.flags or 2
                        user.flags2 = user.flags2 or 256
                    }

                    user.profileColor = profileColor
                    user.color = color
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed after ${currentUserMethod.name}", throwable)
            }
            result
        }
    }
}
