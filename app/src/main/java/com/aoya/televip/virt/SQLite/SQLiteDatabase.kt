package com.aoya.televip.virt.sqlite

import com.aoya.televip.virt.sqlite.SQLiteCursor
import de.robv.android.xposed.XposedHelpers.callMethod
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class SQLiteDatabase(
    private val instance: Any,
) {
    private val objPath = "org.telegram.SQLite.SQLiteDatabase"

    fun queryFinalized(
        sql: String,
        vararg args: Any,
    ): SQLiteCursor =
        SQLiteCursor(
            callMethod(
                instance,
                resolver.getMethod(objPath, "queryFinalized"),
                sql,
                *args,
            ),
        )
}
