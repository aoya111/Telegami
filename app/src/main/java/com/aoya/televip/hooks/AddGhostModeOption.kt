package com.aoya.televip.hooks

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.widget.CheckBox
import android.widget.LinearLayout
import com.aoya.televip.TeleVip
import com.aoya.televip.core.Config
import com.aoya.televip.core.Constants
import com.aoya.televip.core.User
import com.aoya.televip.ui.AlertDialogBuilder
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.newInstance
import java.util.ArrayList
import com.aoya.televip.core.i18n.TranslationManager as i18n

class AddGhostModeOption :
    Hook(
        "add_ghost_mode_option",
        "Add ghost mode int the navigation drawer",
    ) {
    val itemID = 13048

    override fun init() {
        findClass(
            "org.telegram.messenger.UserConfig",
        ).hook("setCurrentUser", HookStage.AFTER) { param ->
            val tgUser = param.arg<Any>(0)

            val user =
                User(
                    getLongField(tgUser, "id"),
                    getObjectField(tgUser, "username") as String,
                    getObjectField(tgUser, "phone") as String,
                )

            Config.initialize(TeleVip.packageName, user)
        }

        findClass("org.telegram.ui.Adapters.DrawerLayoutAdapter").hook("resetItems", HookStage.AFTER) { param ->
            @Suppress("UNCHECKED_CAST")
            val items = getObjectField(param.thisObject(), "items") as ArrayList<Any>

            val settingsIcon =
                items
                    .filterNotNull()
                    .find {
                        getIntField(it, "id") == 8
                    }?.let {
                        getIntField(it, "icon")
                    }

            val newItem =
                newInstance(
                    findClass("org.telegram.ui.Adapters.DrawerLayoutAdapter\$Item"),
                    itemID,
                    i18n.get("ghost_mode"),
                    settingsIcon,
                ) as Any

            items.add(newItem)
        }

        findClass("org.telegram.ui.LaunchActivity").hook("lambda\$onCreate\$6", HookStage.AFTER) { param ->
            val o = param.thisObject() as Activity

            val result = callMethod(getObjectField(o, "drawerLayoutAdapter") ?: return@hook, "getId", param.arg<Int>(1)) as Int
            if (result == itemID) {
                val layout = LinearLayout(o)
                layout.setOrientation(LinearLayout.VERTICAL)
                val checkBoxes = mutableListOf<CheckBox>()
                val opts = Constants.FEATURES.associateWith { i18n.get(it) }
                for ((k, v) in opts) {
                    val checkBox = CheckBox(o)

                    checkBox.text = v
                    checkBox.isChecked = Config.isHookEnabled(k)
                    checkBox.setTextColor(if (isDark) Color.WHITE else Color.BLACK)
                    checkBox.setPadding(10, 10, 10, 10)
                    checkBox.setTypeface(Typeface.DEFAULT_BOLD)

                    checkBoxes.add(checkBox)
                    layout.addView(checkBox)
                }
                AlertDialogBuilder(o)
                    .setTitle(i18n.get("ghost_mode_title"))
                    .setView(layout)
                    .setPositiveButton(i18n.get("save")) { dialog ->
                        try {
                            checkBoxes.forEach { chkBx ->
                                val text = chkBx.text.toString()
                                val key = opts.entries.find { it.value == text }?.key ?: return@forEach
                                Config.setHookEnabled(key, chkBx.isChecked)
                            }
                            dialog.dismiss()
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }.setNegativeButton(i18n.get("developer_channel")) { dialog ->
                        try {
                            val drawerLayoutContainer = getObjectField(param.thisObject(), "drawerLayoutContainer")
                            if (drawerLayoutContainer != null) {
                                callStaticMethod(
                                    findClass("org.telegram.messenger.browser.Browser"),
                                    "openUrl",
                                    o,
                                    "https://t.me/t_l0_e",
                                )
                                dialog.dismiss()
                            }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }.show()
            }
        }
    }
}
