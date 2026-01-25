package com.aoya.telegami.hooks

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.widget.CheckBox
import android.widget.LinearLayout
import com.aoya.telegami.Telegami
import com.aoya.telegami.core.Config
import com.aoya.telegami.core.Constants
import com.aoya.telegami.core.User
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.messenger.browser.Browser
import com.aoya.telegami.virt.tgnet.TLRPC
import com.aoya.telegami.virt.ui.LaunchActivity
import com.aoya.telegami.virt.ui.actionbar.AlertDialog
import com.aoya.telegami.virt.ui.adapters.DrawerLayoutAdapter
import java.util.ArrayList
import com.aoya.telegami.core.i18n.TranslationManager as i18n

class Settings :
    Hook(
        "settings",
        "Add extra settings in the navigation drawer",
    ) {
    val itemID = 13048

    override fun init() {
        findAndHook("org.telegram.messenger.UserConfig", "setCurrentUser", HookStage.AFTER, filter = { true }) { param ->
            val tgUser = TLRPC.User(param.arg<Any>(0))
            val user = User(tgUser.id, tgUser.username, tgUser.phone)
            Config.initialize(Telegami.packageName, user)
        }

        findAndHook("org.telegram.ui.Adapters.DrawerLayoutAdapter", "resetItems", HookStage.AFTER, filter = { true }) { param ->
            val o = DrawerLayoutAdapter(param.thisObject())
            val items = o.items
            val settingsIcon =
                items
                    .filterNotNull()
                    .find {
                        it.id == 8
                    }?.let {
                        it.icon
                    } ?: return@findAndHook
            val newItem = DrawerLayoutAdapter.Item(itemID, "${i18n.get("AppName")} ${getStringResource("Settings")}", settingsIcon)
            o.addItem(newItem)
        }

        findAndHook("org.telegram.ui.LaunchActivity", "lambda\$onCreate\$6", HookStage.AFTER, filter = { true }) { param ->
            val o = LaunchActivity(param.thisObject())

            val result = o.drawerLayoutAdapter?.getId(param.arg<Int>(1))
            if (result == itemID) {
                val layout = LinearLayout(o.context)
                layout.setOrientation(LinearLayout.VERTICAL)
                val checkBoxes = mutableListOf<CheckBox>()
                val opts = Constants.getFeaturesForPackage(Telegami.packageName).associateWith { i18n.get(it) }
                for ((k, v) in opts) {
                    val checkBox = CheckBox(o.context)

                    checkBox.text = v
                    checkBox.isChecked = Config.isEnabled(k)
                    checkBox.setTextColor(if (isDark) Color.WHITE else Color.BLACK)
                    checkBox.setPadding(10, 10, 10, 10)
                    checkBox.setTypeface(Typeface.DEFAULT_BOLD)

                    checkBoxes.add(checkBox)
                    layout.addView(checkBox)
                }
                AlertDialog
                    .Builder(o.context)
                    .setTitle(getStringResource("Settings"))
                    .setView(layout)
                    .setPositiveButton(getStringResource("Save")) { dialog ->
                        try {
                            checkBoxes.forEach { chkBx ->
                                val text = chkBx.text.toString()
                                val key = opts.entries.find { it.value == text }?.key ?: return@forEach
                                val oldState = Config.isEnabled(key)
                                val isChecked = chkBx.isChecked
                                if (isChecked != oldState) {
                                    Config.setHookEnabled(key, isChecked)
                                }
                            }
                            dialog.dismiss()
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }.setNegativeButton("App Channel") { dialog ->
                        try {
                            if (o.drawerLayoutContainer != null) {
                                Browser.openUrl(o.context, "https://t.me/TGamiApp")
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
