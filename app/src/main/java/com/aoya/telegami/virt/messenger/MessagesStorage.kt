package com.aoya.telegami.virt.messenger

import com.aoya.telegami.virt.sqlite.SQLiteDatabase
import de.robv.android.xposed.XposedHelpers.getObjectField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class MessagesStorage(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.MessagesStorage"

    val database: SQLiteDatabase
        get() = SQLiteDatabase(getObjectField(instance, resolver.getField(objPath, "database")))
}
