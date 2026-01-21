package com.aoya.telegami.virt.sqlite

import de.robv.android.xposed.XposedHelpers.callMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class SQLiteCursor(
    private val instance: Any,
) : AutoCloseable {
    private val objPath = "org.telegram.SQLite.SQLiteCursor"

    private val nextMethod by lazy { resolver.getMethod(objPath, "next") }
    private val intValueMethod by lazy { resolver.getMethod(objPath, "intValue") }
    private val longValueMethod by lazy { resolver.getMethod(objPath, "longValue") }

    fun next(): Boolean = callMethod(instance, nextMethod) as Boolean

    fun intValue(colIdx: Int): Int = callMethod(instance, intValueMethod, colIdx) as Int

    fun longValue(colIdx: Int): Long = callMethod(instance, longValueMethod, colIdx) as Long

    override fun close() {
        callMethod(instance, resolver.getMethod(objPath, "dispose"))
    }
}
