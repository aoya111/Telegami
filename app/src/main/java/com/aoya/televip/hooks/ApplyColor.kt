package com.aoya.televip.hooks

import android.app.Activity
import android.content.Context
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import com.aoya.televip.virt.tgnet.TLRPC
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.getStaticObjectField

class ApplyColor :
    Hook(
        "apply_color",
        "Apply selected color and emoji on profile and name",
    ) {
    override fun init() {
        try {
            findClass("org.telegram.ui.PeerColorActivity").hook("apply", HookStage.AFTER) { param ->
                val o = param.thisObject()
                val appCtx =
                    getStaticObjectField(
                        findClass("org.telegram.messenger.ApplicationLoader"),
                        "applicationContext",
                    ) as Context

                data class Page(
                    val name: String = "",
                    val pref: String = "",
                )
                for (p in listOf(Page("profilePage", "teleProfilePage"), Page("namePage", "teleNamePage"))) {
                    val pref = appCtx.getSharedPreferences(p.pref, Activity.MODE_PRIVATE)
                    val page = getObjectField(o, p.name) ?: continue
                    var selectedColor = getIntField(page, "selectedColor")
                    var selectedEmoji = getLongField(page, "selectedEmoji")
                    if (selectedColor != 0) {
                        pref.edit().putString("selectedColor", selectedColor.toString()).commit()
                    } else {
                        pref.edit().remove("selectedColor").commit()
                    }
                    if (selectedEmoji != 0L) {
                        pref.edit().putString("selectedEmoji", selectedEmoji.toString()).commit()
                    } else {
                        pref.edit().remove("selectedEmoji").commit()
                    }
                }
            }

            findClass("org.telegram.messenger.UserConfig").hook("getCurrentUser", HookStage.AFTER) { param ->
                val user = TLRPC.User(param.getResult() ?: return@hook)

                var profileColor = user.profileColor
                if (profileColor == null) {
                    profileColor = TLRPC.TLPeerColor()
                    user.profileColor = profileColor
                }
                var color = user.color
                if (color == null) {
                    color = TLRPC.TLPeerColor()
                    user.color = color
                    val color2 = user.id % 7
                    color.color = color2.toInt()
                }

                val appCtx =
                    getStaticObjectField(
                        findClass("org.telegram.messenger.ApplicationLoader"),
                        "applicationContext",
                    ) as Context

                data class Page(
                    val pref: String = "",
                    val peerColor: TLRPC.PeerColor,
                    val colFlag: Int = 0,
                    val emojiFlag: Int = 0,
                    val flags2: Int = 0,
                )
                for (p in listOf(Page("teleProfilePage", profileColor, 1, 2, 512), Page("teleNamePage", color, 1, 2, 256))) {
                    val pref = appCtx.getSharedPreferences(p.pref, Activity.MODE_PRIVATE)
                    if (pref.contains("selectedColor")) {
                        val selectedColor = pref.getString("selectedColor", "0")?.toInt() ?: 0
                        p.peerColor.color = selectedColor
                        val flags = p.peerColor.flags or p.colFlag
                        p.peerColor.flags = flags

                        val flags2 = user.flags2 or p.flags2
                        user.flags2 = flags2
                    }
                    if (pref.contains("selectedEmoji")) {
                        val selectedEmoji = pref.getString("selectedEmoji", "0")?.toLong() ?: 0L
                        p.peerColor.backgroundEmojiId = selectedEmoji
                        val flags = p.peerColor.flags or p.emojiFlag
                        p.peerColor.flags = flags

                        val flags2 = user.flags2 or p.flags2
                        user.flags2 = flags2
                    }
                }
            }
        } catch (e: ClassNotFoundException) {
            XposedBridge.log("Class not found: ${e.message}")
            e.printStackTrace()
        }
    }
}
