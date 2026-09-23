package com.aoya.telegami.virt.tgnet

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class RequestDelegate(
    private val instance: Any,
) {
    private val objPath = "org.telegram.tgnet.RequestDelegate"

    fun run(
        res: Any,
        error: Any?,
    ) {
        instance
            .asResolver()
            .firstMethod {
                name = resolver.getMethod(objPath, "run")
            }.invoke(res, error)
    }
}
