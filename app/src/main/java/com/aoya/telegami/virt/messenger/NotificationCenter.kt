package com.aoya.telegami.virt.messenger

import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class NotificationCenter {
    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.NotificationCenter"

        const val MESSAGES_DELETED = 7
    }
}
