package com.aoya.telegami.virt.sqlite

import com.aoya.telegami.virt.sqlite.SQLiteCursor
import de.robv.android.xposed.XposedHelpers.callMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

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
