package com.aoya.televip.virt.messenger

import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getLongField
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class UserConfig(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.UserConfig"

    val userId: Long
        get() = getLongField(instance, "userId")

    fun getClientUserId(): Long? =
        callMethod(
            instance,
            resolver.getMethod(objPath, "getClientUserId"),
        ) as? Long
}
