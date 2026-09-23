package com.aoya.telegami.virt.sqlite

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.virt.sqlite.SQLiteCursor
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class SQLiteDatabase(
    private val instance: Any,
) {
    private val objPath = "org.telegram.SQLite.SQLiteDatabase"

    fun queryFinalized(
        sql: String,
        vararg args: Any,
    ): SQLiteCursor? = instance.asResolver().firstMethod { this.name = resolver.getMethod(objPath, "queryFinalized"); parameters(*arrayOf(sql?.javaClass ?: Any::class.java, args?.javaClass ?: Any::class.java)) }.invoke(sql, args)!!?.let { SQLiteCursor(it) }
}
