package com.aoya.telegami.hooks

import android.view.WindowManager.LayoutParams
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import com.aoya.telegami.util.findMethod
import android.util.Log
import io.github.libxposed.api.XposedInterface

object AllowScreenshots {
    const val WINDOW_CN = "android.view.Window"
    const val WINDOW_MANAGER_IMPL_CN = "android.view.WindowManagerImpl"

    fun install(xposed: XposedInterface, classLoader: ClassLoader) {
        val setFlags = classLoader.findMethod(WINDOW_CN, "setFlags")
        xposed.hook(setFlags).intercept { chain ->
            val args = chain.args.toMutableList()
            try {
                args[0] = (args[0] as Int) and FLAG_SECURE.inv()
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed before ${setFlags.name}", throwable)
            }
            chain.proceed(args.toTypedArray())
        }
        val addView = classLoader.findMethod(WINDOW_MANAGER_IMPL_CN, "addView")
        xposed.hook(addView).intercept { chain ->
            val args = chain.args.toMutableList()
            try {
                val layoutParams = args[1] as? LayoutParams
                if (layoutParams != null) layoutParams.flags = layoutParams.flags and FLAG_SECURE.inv()
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed before ${addView.name}", throwable)
            }
            chain.proceed(args.toTypedArray())
        }
    }
}
