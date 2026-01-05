package com.aoya.televip.virt.messenger

import com.aoya.televip.virt.sqlite.SQLiteDatabase
import de.robv.android.xposed.XposedHelpers.getObjectField
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class MessagesStorage(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.MessagesStorage"

    val database: SQLiteDatabase
        get() = SQLiteDatabase(getObjectField(instance, resolver.getField(objPath, "database")))
}
