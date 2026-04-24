package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.service.Config
import com.aoya.telegami.service.UserConfig
import com.aoya.telegami.virt.tgnet.TLRPC
import com.aoya.telegami.virt.ui.PeerColorActivity
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object ApplyTheme : YukiBaseHooker() {
    const val PEER_COLOR_ACTIVITY_CN = "org.telegram.ui.PeerColorActivity"
    const val USER_CONFIG_CN = "org.telegram.messenger.UserConfig"
    val peerColorActivityClass by lazyClass(resolver.get(PEER_COLOR_ACTIVITY_CN))
    val userConfigClass by lazyClass(resolver.get(USER_CONFIG_CN))

    override fun onHook() {
        if (Telegami.packageName in listOf("uz.unnarsx.cherrygram", "xyz.nextalone.nagram")) return
        if (!Config.isFeatureEnabled("UnlockChannelFeatures")) return
        peerColorActivityClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(PEER_COLOR_ACTIVITY_CN, "apply")
            }.hook {
                after {
                    val o = instance?.let { PeerColorActivity(it) } ?: return@after

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
                }
            }
        userConfigClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(USER_CONFIG_CN, "getCurrentUser")
            }.hook {
                after {
                    val user = result?.let { TLRPC.User(it) } ?: return@after

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
                }
            }
    }
}
