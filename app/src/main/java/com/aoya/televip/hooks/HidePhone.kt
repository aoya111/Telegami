package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedHelpers.setObjectField

class HidePhoneNumber :
    Hook(
        "hide_phone_number",
        "Hide 'Phone' number",
    ) {
    override fun init() {
        findClass(
            "org.telegram.messenger.UserConfig",
        ).hook("getCurrentUser", HookStage.AFTER) { param ->
            val user = param.getResult() ?: return@hook
            setObjectField(user, "phone", null)
        }
    }
}
