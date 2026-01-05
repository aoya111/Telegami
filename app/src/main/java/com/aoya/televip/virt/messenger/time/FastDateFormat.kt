
package com.aoya.televip.virt.messenger.time

import de.robv.android.xposed.XposedHelpers.callMethod
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class FastDateFormat(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.time.FastDateFormat"

    fun format(millis: Long) = callMethod(instance, resolver.getMethod(objPath, "format"), millis) as String
}
