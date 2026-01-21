package com.aoya.telegami.virt.messenger.browser

import android.content.Context
import com.aoya.telegami.Telegami
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class Browser {
    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.browser.Browser"

        fun openUrl(
            ctx: Context,
            url: String,
        ): Unit =
            callStaticMethod(
                Telegami.loadClass(resolver.get(OBJ_PATH)),
                resolver.getMethod(OBJ_PATH, "openUrl"),
                ctx,
                url,
            ) as Unit
    }
}
