package com.aoya.televip.virt.sqlite

import de.robv.android.xposed.XposedHelpers.callMethod
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class SQLiteCursor(
    private val instance: Any,
) {
    private val objPath = "org.telegram.SQLite.SQLiteCursor"

    fun next(): Boolean = callMethod(instance, resolver.getMethod(objPath, "next")) as Boolean

    fun intValue(colIdx: Int): Int = callMethod(instance, resolver.getMethod(objPath, "intValue"), colIdx) as Int

    fun longValue(colIdx: Int): Long = callMethod(instance, resolver.getMethod(objPath, "longValue"), colIdx) as Long
}
