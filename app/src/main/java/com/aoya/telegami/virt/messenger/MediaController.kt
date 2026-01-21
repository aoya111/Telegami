package com.aoya.telegami.virt.messenger

import android.content.Context
import android.net.Uri
import com.aoya.telegami.Telegami
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import java.lang.reflect.Proxy.newProxyInstance
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class MediaController {
    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.MediaController"

        fun saveFile(
            fullPath: String,
            context: Context,
            type: Int,
            name: String?,
            mime: String?,
            showProgress: Boolean = true,
            onSaved: ((Uri?) -> Unit)? = null,
        ): Any? {
            val callbackClass = Telegami.loadClass(resolver.get("org.telegram.messenger.Utilities\$Callback"))
            val callback =
                onSaved?.let { lambda ->
                    newProxyInstance(
                        callbackClass.classLoader,
                        arrayOf(callbackClass),
                    ) { _, method, args ->
                        if (method.name == resolver.getMethod("org.telegram.messenger.Utilities\$Callback", "run")) {
                            lambda(args?.firstOrNull() as? Uri)
                        }
                        null
                    }
                }

            return callStaticMethod(
                Telegami.loadClass(resolver.get(OBJ_PATH)),
                resolver.getMethod(OBJ_PATH, "saveFile"),
                fullPath,
                context,
                type,
                name,
                mime,
                callback,
                showProgress,
            )
        }
    }
}
