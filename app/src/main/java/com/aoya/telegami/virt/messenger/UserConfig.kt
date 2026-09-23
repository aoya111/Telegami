package com.aoya.telegami.virt.messenger

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class UserConfig(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.UserConfig"

    val userId: Long
        get() = instance.asResolver().firstField { this.name = "userId" }.get<Long>()!!

    fun getClientUserId(): Long =
        instance.asResolver().firstMethod { this.name = resolver.getMethod(objPath, "getClientUserId"); parameters() }.invoke()!! as? Long ?: 0L
}
