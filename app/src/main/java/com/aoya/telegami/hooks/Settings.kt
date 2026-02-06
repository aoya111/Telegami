package com.aoya.telegami.hooks

import android.app.Activity
import android.content.Context
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
import com.aoya.telegami.virt.ui.SettingsActivity
import com.aoya.telegami.virt.ui.actionbar.AlertDialog
import com.aoya.telegami.virt.ui.adapters.DrawerLayoutAdapter
import com.aoya.telegami.virt.ui.components.UItem
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

        fun showAlert(ctx: Context) {
            val layout = LinearLayout(ctx)
            layout.setOrientation(LinearLayout.VERTICAL)
            val checkBoxes = mutableListOf<CheckBox>()
            val opts = Constants.getFeaturesForPackage(Telegami.packageName).associateWith { i18n.get(it) }
            for ((k, v) in opts) {
                val checkBox = CheckBox(ctx)

                checkBox.text = v
                checkBox.isChecked = Config.isHookEnabled(k)
                checkBox.setTextColor(if (isDark) Color.WHITE else Color.BLACK)
                checkBox.setPadding(10, 10, 10, 10)
                checkBox.setTypeface(Typeface.DEFAULT_BOLD)

                checkBoxes.add(checkBox)
                layout.addView(checkBox)
            }
            AlertDialog
                .Builder(ctx)
                .setTitle(getStringResource("Settings"))
                .setView(layout)
                .setPositiveButton(getStringResource("Save")) { dialog ->
                    try {
                        checkBoxes.forEach { chkBx ->
                            val text = chkBx.text.toString()
                            val key = opts.entries.find { it.value == text }?.key ?: return@forEach
                            val oldState = Config.isHookEnabled(key)
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
                        Browser.openUrl(ctx, "https://t.me/TGamiApp")
                        dialog.dismiss()
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }.show()
        }

        val settingsLabel = "${i18n.get("AppName")} ${getStringResource("Settings")}"
        val settingsIcon = getResource("msg_settings_old", "drawable")

        if (Telegami.packageName == "org.telegram.messenger.beta") {
            findAndHook("org.telegram.ui.SettingsActivity", "fillItems", HookStage.AFTER, filter = { true }) { param ->
                val arrayList = param.arg<Any>(0) as ArrayList<Any?>
                val item =
                    SettingsActivity.SettingCell.Factory.of(
                        itemID,
                        -13451058,
                        -14836538,
                        settingsIcon,
                        settingsLabel,
                        "",
                    )
                arrayList.add(10, item)
            }
            findAndHook("org.telegram.ui.SettingsActivity", "onClick", HookStage.AFTER, filter = { true }) { param ->
                val o = SettingsActivity(param.thisObject())
                val ctx = o.getContext()
                val uItem = UItem(param.arg<Any>(0))

                if (uItem.id == itemID) {
                    showAlert(ctx)
                }
            }
        } else {
            findAndHook("org.telegram.ui.Adapters.DrawerLayoutAdapter", "resetItems", HookStage.AFTER, filter = { true }) { param ->
                val o = DrawerLayoutAdapter(param.thisObject())
                val insertIdx = o.items.indexOfFirst { it?.let { it.id == 8 } ?: false }
                val newItem = DrawerLayoutAdapter.Item(itemID, settingsLabel, settingsIcon)
                if (insertIdx != -1) {
                    o.addItem(insertIdx + 1, newItem)
                } else {
                    o.addItem(newItem)
                }
            }
            findAndHook("org.telegram.ui.LaunchActivity", "lambda\$onCreate\$6", HookStage.AFTER, filter = { true }) { param ->
                val o = LaunchActivity(param.thisObject())

                val result = o.drawerLayoutAdapter?.getId(param.arg<Int>(1))
                if (result == itemID) {
                    showAlert(o.context)
                }
            }
        }
    }
}
