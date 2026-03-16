package com.aoya.telegami.virt.messenger

import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.tgnet.TLRPC
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class AndroidUtilities {
    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.AndroidUtilities"

        private val classAndroidUtilities by lazy { Telegami.loadClass(resolver.get(OBJ_PATH)) }
        private val methodAddToClipboard by lazy { resolver.getMethod(OBJ_PATH, "addToClipboard") }
        private val methodDp by lazy { resolver.getMethod(OBJ_PATH, "dp") }

        fun addToClipboard(text: String): Unit? =
            callStaticMethod(
                classAndroidUtilities,
                methodAddToClipboard,
                text,
            ) as? Unit

        fun dp(value: Float): Int =
            callStaticMethod(
                classAndroidUtilities,
                methodDp,
                value,
            ) as Int
    }
}
