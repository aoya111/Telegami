package com.aoya.telegami.hooks

import android.view.WindowManager.LayoutParams
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.hook

class AllowScreenshots :
    Hook(
        "allow_screenshots",
        "Allow screenshots everywhere in the app",
    ) {
    override fun init() {
        findClass("android.view.Window").hook("setFlags", HookStage.BEFORE) { param ->
            if (!isEnabled) return@hook
            var flags = param.arg<Int>(0)
            flags = flags and FLAG_SECURE.inv()
            param.setArg(0, flags)
        }

        findClass("android.view.WindowManagerImpl").hook("addView", HookStage.BEFORE) { param ->
            if (!isEnabled) return@hook
            val layoutParams = param.arg<LayoutParams>(1)

            if ((layoutParams.flags and FLAG_SECURE) != 0) {
                layoutParams.flags = layoutParams.flags and FLAG_SECURE.inv()
            }
        }
    }
}
