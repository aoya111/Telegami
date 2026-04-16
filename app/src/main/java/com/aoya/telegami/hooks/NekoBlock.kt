package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object NekoBlock : YukiBaseHooker() {
    val suspiciousClass by lazyClass("uo5")

    override fun onHook() {
        if (Telegami.packageName != "tw.nekomimi.nekogram") return
        suspiciousClass
            .resolve()
            .firstMethod {
                name = "g"
            }.hook {
                before {
                    resultNull()
                }
            }
    }
}

// class NekoBlock : Hook("NekoBlock") {
//     override fun init() {
//         if (Telegami.packageName != "tw.nekomimi.nekogram") return
//         findAndHook("uo5", "g", HookStage.BEFORE, filter = { true }) { param ->
//             logd("Neko is spying")
//             param.setResult(null)
//         }
//     }
// }
