package com.aoya.telegami.virt.messenger

import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.tgnet.TLRPC
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class AndroidUtilities {
    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.AndroidUtilities"

        private val classAndroidUtilities by lazy { Telegami.loadClass(resolver.get(OBJ_PATH)) }
        private val methodAddToClipboard by lazy { resolver.getMethod(OBJ_PATH, "addToClipboard") }
        private val methodDp by lazy { resolver.getMethod(OBJ_PATH, "dp") }

        fun addToClipboard(text: CharSequence) {
            (classAndroidUtilities as Class<Any>)
                .resolve()
                .firstMethod {
                    name = methodAddToClipboard
                    parameters(CharSequence::class.java)
                }.invoke(text)
        }

        fun dp(value: Float): Int =
            (classAndroidUtilities as Class<Any>)
                .resolve()
                .firstMethod {
                    name = methodDp
                    parameters(Float::class.javaPrimitiveType!!)
                }.invoke(value)!! as Int
    }
}
