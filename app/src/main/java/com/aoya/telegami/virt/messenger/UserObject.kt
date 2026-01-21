package com.aoya.telegami.virt.messenger

import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.tgnet.TLRPC
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class UserObject {
    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.UserObject"

        fun getPublicUsername(user: TLRPC.User): String? =
            callStaticMethod(
                Telegami.loadClass(resolver.get(OBJ_PATH)),
                resolver.getMethod(OBJ_PATH, "getPublicUsername"),
                user.getNativeInstance(),
            ) as? String
    }
}
