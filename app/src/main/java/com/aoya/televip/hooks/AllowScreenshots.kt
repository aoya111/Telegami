package com.aoya.televip.hooks

import android.view.WindowManager.LayoutParams
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook

class AllowScreenshots :
    Hook(
        "Allow screenshots",
        "Allow screenshots everywhere in the app",
    ) {
    override fun init() {
        findClass("android.view.Window").hook("setFlags", HookStage.BEFORE) { param ->
            var flags = param.arg<Int>(0)
            flags = flags and FLAG_SECURE.inv()
            param.setArg(0, flags)
        }

        findClass("android.view.WindowManagerImpl").hook("addView", HookStage.BEFORE) { param ->
            val layoutParams = param.arg<LayoutParams>(1)

            if ((layoutParams.flags and FLAG_SECURE) != 0) {
                layoutParams.flags = layoutParams.flags and FLAG_SECURE.inv()
            }
        }
    }
}
