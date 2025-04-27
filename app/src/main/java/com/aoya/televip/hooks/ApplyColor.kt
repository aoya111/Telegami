package com.aoya.televip.hooks

import android.app.Activity
import android.content.Context
import com.aoya.televip.TeleVip
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.getStaticObjectField
import de.robv.android.xposed.XposedHelpers.newInstance
import de.robv.android.xposed.XposedHelpers.setIntField
import de.robv.android.xposed.XposedHelpers.setLongField
import de.robv.android.xposed.XposedHelpers.setObjectField

class ApplyColor :
    Hook(
        "apply_color",
        "Apply selected color and emoji on profile and name",
    ) {
    override fun init() {
        try {
            if (TeleVip.packageName !in listOf("com.skyGram.bestt", "xyz.nextalone.nagram")) {
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
                        val page = getObjectField(o, p.name)
                        if (page == null) continue
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
                    val user = param.getResult() ?: return@hook

                    var profileColor = getObjectField(user, "profile_color")
                    if (profileColor == null) {
                        profileColor = newInstance(findClass("org.telegram.tgnet.TLRPC\$TL_peerColor"))
                        setObjectField(user, "profile_color", profileColor)
                    }
                    var color = getObjectField(user, "color")
                    if (color == null) {
                        color = newInstance(findClass("org.telegram.tgnet.TLRPC\$TL_peerColor"))
                        setObjectField(user, "color", color)
                        val color2 = getLongField(user, "id") % 7
                        setObjectField(color, "color", color2.toInt())
                    }

                    val appCtx =
                        getStaticObjectField(
                            findClass("org.telegram.messenger.ApplicationLoader"),
                            "applicationContext",
                        ) as Context

                    data class Page(
                        val pref: String = "",
                        val peerColor: Any,
                        val colFlag: Int = 0,
                        val emojiFlag: Int = 0,
                        val flags2: Int = 0,
                    )
                    for (p in listOf(Page("teleProfilePage", profileColor, 1, 2, 512), Page("teleNamePage", color, 1, 2, 256))) {
                        val pref = appCtx.getSharedPreferences(p.pref, Activity.MODE_PRIVATE)
                        if (pref.contains("selectedColor")) {
                            val selectedColor = pref.getString("selectedColor", "0")?.toInt() ?: 0
                            setIntField(p.peerColor, "color", selectedColor)
                            val flags = getIntField(p.peerColor, "flags") or p.colFlag
                            setIntField(p.peerColor, "flags", flags)

                            val flags2 = getIntField(user, "flags2") or p.flags2
                            setIntField(user, "flags2", flags2)
                        }
                        if (pref.contains("selectedEmoji")) {
                            val selectedEmoji = pref.getString("selectedEmoji", "0")?.toLong() ?: 0L
                            setLongField(p.peerColor, "background_emoji_id", selectedEmoji)
                            val flags = getIntField(p.peerColor, "flags") or p.emojiFlag
                            setIntField(p.peerColor, "flags", flags)

                            val flags2 = getIntField(user, "flags2") or p.flags2
                            setIntField(user, "flags2", flags2)
                        }
                    }
                }
            }
        } catch (e: ClassNotFoundException) {
            XposedBridge.log("Class not found: ${e.message}")
            e.printStackTrace()
        }
    }
}
