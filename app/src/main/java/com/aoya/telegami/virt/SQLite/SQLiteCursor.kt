package com.aoya.telegami.virt.sqlite

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class SQLiteCursor(
    private val instance: Any,
) : AutoCloseable {
    private val objPath = "org.telegram.SQLite.SQLiteCursor"

    private val nextMethod by lazy { resolver.getMethod(objPath, "next") }
    private val intValueMethod by lazy { resolver.getMethod(objPath, "intValue") }
    private val longValueMethod by lazy { resolver.getMethod(objPath, "longValue") }

    fun next(): Boolean = instance.asResolver().firstMethod { this.name = nextMethod; parameters() }.invoke()!! as Boolean

    fun intValue(colIdx: Int): Int = instance.asResolver().firstMethod { this.name = intValueMethod; parameters(*arrayOf(colIdx?.javaClass ?: Any::class.java)) }.invoke(colIdx)!! as Int

    fun longValue(colIdx: Int): Long = instance.asResolver().firstMethod { this.name = longValueMethod; parameters(*arrayOf(colIdx?.javaClass ?: Any::class.java)) }.invoke(colIdx)!! as Long

    override fun close() {
        instance.asResolver().firstMethod { this.name = resolver.getMethod(objPath, "dispose"); parameters() }.invoke()!!
    }
}
