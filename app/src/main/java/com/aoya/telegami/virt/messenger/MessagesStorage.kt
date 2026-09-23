package com.aoya.telegami.virt.messenger

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.virt.sqlite.SQLiteDatabase
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class MessagesStorage(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.MessagesStorage"

    val database: SQLiteDatabase
        get() = SQLiteDatabase(instance.asResolver().firstField { this.name = resolver.getField(objPath, "database") }.get()!!)
}
