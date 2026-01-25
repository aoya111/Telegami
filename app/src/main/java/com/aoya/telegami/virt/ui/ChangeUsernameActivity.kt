package com.aoya.telegami.virt.ui

import com.aoya.telegami.Telegami
import de.robv.android.xposed.XposedHelpers.newInstance
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ChangeUsernameActivity(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    companion object {
        private const val OBJ_PATH = "org.telegram.ui.ChangeUsernameActivity"

        fun newInstance(): ChangeUsernameActivity =
            ChangeUsernameActivity(newInstance(Telegami.loadClass("org.telegram.ui.ChangeUsernameActivity")))
    }

    fun getNativeInstance() = instance
}
