package com.aoya.telegami.virt.messenger

import com.aoya.telegami.Telegami
import de.robv.android.xposed.XposedHelpers.getStaticIntField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class NotificationCenter {
    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.NotificationCenter"

        val MESSAGES_DELETED: Int
            get() =
                getStaticIntField(
                    Telegami.loadClass(resolver.get(OBJ_PATH)),
                    resolver.getField(OBJ_PATH, "messagesDeleted"),
                ) as Int
    }
}
