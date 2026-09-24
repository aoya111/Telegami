package com.aoya.telegami.virt.messenger

import android.content.Context
import android.net.Uri
import com.aoya.telegami.Telegami
import com.aoya.telegami.util.Logger
import com.highcapable.kavaref.KavaRef.Companion.resolve
import java.lang.reflect.Proxy.newProxyInstance
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class MediaController {
    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.MediaController"

        fun saveFile(
            fullPath: String,
            context: Context,
            type: Int,
            filename: String?,
            mime: String?,
            onSaved: ((Uri?) -> Unit)? = null,
        ) {
            val callbackClass = Telegami.loadClass(resolver.get("org.telegram.messenger.Utilities\$Callback"))
            val callback =
                onSaved?.let { lambda ->
                    newProxyInstance(
                        callbackClass.classLoader,
                        arrayOf(callbackClass),
                    ) { _, method, args ->
                        if (method.name == resolver.getMethod("org.telegram.messenger.Utilities\$Callback", "run")) {
                            try {
                                lambda(args?.firstOrNull() as? Uri)
                            } catch (throwable: Throwable) {
                                Logger.e("Failed to handle saved media", throwable, "MediaController")
                            }
                        }
                        null
                    }
                }

            if (Telegami.packageName in listOf("it.octogram.android", "tw.nekomimi.nekogram")) {
                (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>)
                    .resolve()
                    .firstMethod {
                        name = resolver.getMethod(OBJ_PATH, "saveFile")
                        parameterCount = 7
                    }.invoke(fullPath, context, type, filename, mime, callback, true)
            } else {
                (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>)
                    .resolve()
                    .firstMethod {
                        name = resolver.getMethod(OBJ_PATH, "saveFile")
                        parameterCount = 6
                    }.invoke(fullPath, context, type, filename, mime, callback)
            }
        }
    }
}
