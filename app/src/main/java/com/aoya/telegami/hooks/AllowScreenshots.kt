package com.aoya.telegami.hooks

import android.view.WindowManager.LayoutParams
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object AllowScreenshots : YukiBaseHooker() {
    const val WINDOW_CN = "android.view.Window"
    const val WINDOW_MANAGER_IMPL_CN = "android.view.WindowManagerImpl"

    val windowClass by lazyClass(resolver.get(WINDOW_CN))
    val windowManagerImplClass by lazyClass(resolver.get(WINDOW_MANAGER_IMPL_CN))

    override fun onHook() {
        windowClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(WINDOW_CN, "setFlags")
            }.hook {
                before {
                    var flags = args(0).int()
                    flags = flags and FLAG_SECURE.inv()
                    args(0).set(flags)
                }
            }
        windowManagerImplClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(WINDOW_MANAGER_IMPL_CN, "addView")
            }.hook {
                before {
                    var layoutParams = args(1).cast<LayoutParams>() ?: return@before

                    if ((layoutParams.flags and FLAG_SECURE) != 0) {
                        layoutParams.flags = layoutParams.flags and FLAG_SECURE.inv()
                    }
                }
            }
    }
}
