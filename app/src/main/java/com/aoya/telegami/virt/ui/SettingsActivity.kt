package com.aoya.telegami.virt.ui

import android.content.Context
import com.aoya.telegami.Telegami
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class SettingsActivity(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    fun getContext() = callMethod(instance, resolver.getMethod(objPath, "getContext")) as Context

    companion object {
        private const val OBJ_PATH = "org.telegram.ui.SettingsActivity"
    }

    class SettingCell(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.ui.SettingsActivity\$SettingCell"

        class Factory(
            private val instance: Any,
        ) {
            private val objPath = OBJ_PATH

            companion object {
                private const val OBJ_PATH = "org.telegram.ui.SettingsActivity\$SettingCell\$Factory"

                fun of(
                    id: Int,
                    i2: Int,
                    i3: Int,
                    icon: Int,
                    label: String,
                    desc: String,
                ) = callStaticMethod(
                    Telegami.loadClass(resolver.get(OBJ_PATH)),
                    resolver.getMethod(OBJ_PATH, "of"),
                    id,
                    i2,
                    i3,
                    icon,
                    label,
                    desc,
                )
            }
        }
    }
}
