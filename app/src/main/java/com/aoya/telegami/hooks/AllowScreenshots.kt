package com.aoya.telegami.hooks

import android.view.WindowManager.LayoutParams
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage

class AllowScreenshots :
    Hook(
        "allow_screenshots",
        "Allow screenshots everywhere in the app",
    ) {
    override fun init() {
        findAndHook("android.view.Window", "setFlags", HookStage.BEFORE) { param ->
            var flags = param.arg<Int>(0)
            flags = flags and FLAG_SECURE.inv()
            param.setArg(0, flags)
        }

        findAndHook("android.view.WindowManagerImpl", "addView", HookStage.BEFORE) { param ->
            val layoutParams = param.arg<LayoutParams>(1)

            if ((layoutParams.flags and FLAG_SECURE) != 0) {
                layoutParams.flags = layoutParams.flags and FLAG_SECURE.inv()
            }
        }
    }
}
