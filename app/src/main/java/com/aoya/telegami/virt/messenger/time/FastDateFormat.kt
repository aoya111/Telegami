
package com.aoya.telegami.virt.messenger.time

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class FastDateFormat(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.time.FastDateFormat"

    fun format(millis: Long) = instance.asResolver().firstMethod { this.name = resolver.getMethod(objPath, "format"); parameters(*arrayOf(millis?.javaClass ?: Any::class.java)) }.invoke(millis)!! as String
}
