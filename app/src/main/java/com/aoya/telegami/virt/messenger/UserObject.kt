package com.aoya.telegami.virt.messenger

import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.tgnet.TLRPC
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.VagueType
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class UserObject {
    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.UserObject"

        fun getPublicUsername(user: TLRPC.User): String? =
            (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>)
                .resolve()
                .firstMethod {
                    name = resolver.getMethod(OBJ_PATH, "getPublicUsername")
                    parameters(VagueType)
                }.invoke(user.getNativeInstance())!! as? String
    }
}
