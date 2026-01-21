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
import com.aoya.telegami.utils.hook
import com.aoya.telegami.virt.messenger.browser.Browser
import com.aoya.telegami.virt.tgnet.TLRPC
import com.aoya.telegami.virt.ui.LaunchActivity
import com.aoya.telegami.virt.ui.actionbar.AlertDialog
import com.aoya.telegami.virt.ui.adapters.DrawerLayoutAdapter
import java.util.ArrayList
import com.aoya.telegami.core.i18n.TranslationManager as i18n
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class AddGhostModeOption :
    Hook(
        "add_ghost_mode_option",
        "Add ghost mode int the navigation drawer",
    ) {
    val itemID = 13048

    override fun init() {
        findClass(
            "org.telegram.messenger.UserConfig",
        ).hook(resolver.getMethod("org.telegram.messenger.UserConfig", "setCurrentUser"), HookStage.AFTER) { param ->
            val tgUser = TLRPC.User(param.arg<Any>(0))
            val user = User(tgUser.id, tgUser.username, tgUser.phone)
            Config.initialize(Telegami.packageName, user)
        }

        val drawerLayoutAdapter = "org.telegram.ui.Adapters.DrawerLayoutAdapter"
        findClass(drawerLayoutAdapter).hook(resolver.getMethod(drawerLayoutAdapter, "resetItems"), HookStage.AFTER) { param ->
            val dlAdapter = DrawerLayoutAdapter(param.thisObject())

            val items = dlAdapter.items

            val settingsIcon =
                items
                    .filterNotNull()
                    .find {
                        it.id == 8
                    }?.let {
                        it.icon
                    } ?: return@hook

            val newItem = DrawerLayoutAdapter.Item(itemID, i18n.get("ghost_mode"), settingsIcon)
            dlAdapter.addItem(newItem)
        }

        findClass(
            "org.telegram.ui.LaunchActivity",
        ).hook(resolver.getMethod("org.telegram.ui.LaunchActivity", "lambda\$onCreate\$6"), HookStage.AFTER) { param ->
            val o = LaunchActivity(param.thisObject())

            val result = o.drawerLayoutAdapter?.getId(param.arg<Int>(1))
            if (result == itemID) {
                val layout = LinearLayout(o.context)
                layout.setOrientation(LinearLayout.VERTICAL)
                val checkBoxes = mutableListOf<CheckBox>()
                val opts = Constants.FEATURES.associateWith { i18n.get(it) }
                for ((k, v) in opts) {
                    val checkBox = CheckBox(o.context)

                    checkBox.text = v
                    checkBox.isChecked = Config.isHookEnabled(k)
                    checkBox.setTextColor(if (isDark) Color.WHITE else Color.BLACK)
                    checkBox.setPadding(10, 10, 10, 10)
                    checkBox.setTypeface(Typeface.DEFAULT_BOLD)

                    checkBoxes.add(checkBox)
                    layout.addView(checkBox)
                }
                AlertDialog
                    .Builder(o.context)
                    .setTitle(i18n.get("ghost_mode_title"))
                    .setView(layout)
                    .setPositiveButton(i18n.get("save")) { dialog ->
                        try {
                            checkBoxes.forEach { chkBx ->
                                val text = chkBx.text.toString()
                                val key = opts.entries.find { it.value == text }?.key ?: return@forEach
                                Config.setHookEnabled(key, chkBx.isChecked)
                                if (chkBx.isChecked) {
                                    Telegami.hookManager.loadHook(key)
                                } else {
                                    Telegami.hookManager.unloadHook(key)
                                }
                            }
                            dialog.dismiss()
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }.setNegativeButton(i18n.get("developer_channel")) { dialog ->
                        try {
                            if (o.drawerLayoutContainer != null) {
                                Browser.openUrl(o.context, "${Constants.GITHUB_REPO}/discussions")
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
