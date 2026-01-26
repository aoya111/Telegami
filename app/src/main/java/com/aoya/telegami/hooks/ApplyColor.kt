package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.core.Config
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.tgnet.TLRPC
import com.aoya.telegami.virt.ui.PeerColorActivity

class ApplyColor :
    Hook(
        "apply_color",
        "Apply selected color and emoji on profile and name",
    ) {
    override fun init() {
        if (Telegami.packageName == "xyz.nextalone.nagram") return

        findAndHook("org.telegram.ui.PeerColorActivity", "apply", HookStage.AFTER, filter = { true }) { param ->
            Config.reload()
            val o = PeerColorActivity(param.thisObject())

            o.profilePage.selectedColor
                .takeIf { it != 0 }
                ?.let(Config::setProfileColor)
            o.profilePage.selectedEmoji
                .takeIf { it != 0L }
                ?.let(Config::setProfileEmoji)
            o.namePage.selectedColor
                .takeIf { it != 0 }
                ?.let(Config::setNameColor)
            o.namePage.selectedEmoji
                .takeIf { it != 0L }
                ?.let(Config::setNameEmoji)
        }

        findAndHook("org.telegram.messenger.UserConfig", "getCurrentUser", HookStage.AFTER, filter = { true }) { param ->
            Config.reload()
            val user = TLRPC.User(param.getResult() ?: return@findAndHook)

            val profileColor = user.profileColor ?: TLRPC.TLPeerColor()
            Config.getProfileColor()?.let {
                profileColor.color = it
                profileColor.flags = profileColor.flags or 1
                user.flags2 = user.flags2 or 512
            }
            Config.getProfileEmoji()?.let {
                profileColor.backgroundEmojiId = it
                profileColor.flags = profileColor.flags or 2
                user.flags2 = user.flags2 or 512
            }

            val color = user.color ?: TLRPC.TLPeerColor()
            val color2 = user.id % 7
            color.color = color2.toInt()
            Config.getNameColor()?.let {
                color.color = it
                color.flags = color.flags or 1
                user.flags2 = user.flags2 or 256
            }
            Config.getNameEmoji()?.let {
                color.backgroundEmojiId = it
                color.flags = color.flags or 2
                user.flags2 = user.flags2 or 256
            }

            user.profileColor = profileColor
            user.color = color
        }
    }
}
