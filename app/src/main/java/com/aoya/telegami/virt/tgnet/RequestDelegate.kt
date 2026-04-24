package com.aoya.telegami.virt.tgnet

import de.robv.android.xposed.XposedHelpers.callMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class RequestDelegate(
    private val instance: Any,
) {
    private val objPath = "org.telegram.tgnet.RequestDelegate"

    fun run(
        res: Any,
        error: Any?,
    ) = callMethod(
        instance,
        resolver.getMethod(objPath, "run"),
        res,
        error,
    )
}
