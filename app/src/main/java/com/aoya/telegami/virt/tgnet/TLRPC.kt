package com.aoya.telegami.virt.tgnet

import com.aoya.telegami.Telegami
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.newInstance
import de.robv.android.xposed.XposedHelpers.setIntField
import de.robv.android.xposed.XposedHelpers.setLongField
import de.robv.android.xposed.XposedHelpers.setObjectField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class TLRPC {
    companion object {
        const val MESSAGE_FLAG_EDITED = 0x00008000
    }

    class User(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.tgnet.TLRPC\$User"

        private val fieldId by lazy { resolver.getField(objPath, "id") }
        private val fieldUsername by lazy { resolver.getField(objPath, "username") }
        private val fieldFlags by lazy { resolver.getField(objPath, "flags") }
        private val fieldFlags2 by lazy { resolver.getField(objPath, "flags2") }
        private val fieldColor by lazy { resolver.getField(objPath, "color") }
        private val fieldProfileColor by lazy { resolver.getField(objPath, "profile_color") }

        val id: Long
            get() = getLongField(instance, fieldId)

        val username: String
            get() = (getObjectField(instance, fieldUsername) as? String) ?: ""

        var flags: Int
            get() = getIntField(instance, fieldFlags)
            set(value) = setIntField(instance, fieldFlags, value)

        var flags2: Int
            get() = getIntField(instance, fieldFlags2)
            set(value) = setIntField(instance, fieldFlags2, value)

        var color: PeerColor?
            get() = getObjectField(instance, fieldColor)?.let { TLPeerColor(it) }
            set(value) = setObjectField(instance, fieldColor, value?.getNativeInstance())

        var profileColor: PeerColor?
            get() = getObjectField(instance, fieldProfileColor)?.let { TLPeerColor(it) }
            set(value) = setObjectField(instance, fieldProfileColor, value?.getNativeInstance())

        fun getNativeInstance() = instance
    }

    class Message(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.tgnet.TLRPC\$Message"

        private val fieldDate by lazy { resolver.getField(objPath, "date") }
        private val fieldFlags by lazy { resolver.getField(objPath, "flags") }
        private val fieldTtl by lazy { resolver.getField(objPath, "ttl") }
        private val fieldMedia by lazy { resolver.getField(objPath, "media") }

        val date: Int
            get() = getIntField(instance, fieldDate)

        val flags: Int
            get() = getIntField(instance, fieldFlags)

        var ttl: Int
            get() = getIntField(instance, fieldTtl)
            set(value) = setIntField(instance, fieldTtl, value)

        val media: MessageMedia
            get() = MessageMedia(getObjectField(instance, fieldMedia))

        fun getNativeInstance() = instance
    }

    class MessageMedia(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.tgnet.TLRPC\$MessageMedia"

        private val fieldTtl by lazy { resolver.getField(objPath, "ttl") }
        private val fieldMedia by lazy { resolver.getField(objPath, "media") }

        var ttl: Int
            get() = getIntField(instance, fieldTtl)
            set(value) = setIntField(instance, fieldTtl, value)

        val media: Any?
            get() = getObjectField(instance, fieldMedia)

        fun getNativeInstance() = instance
    }

    class Chat(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.tgnet.TLRPC\$Chat"

        fun getNativeInstance() = instance
    }

    abstract class PeerColor(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.tgnet.TLRPC\$PeerColor"

        private val fieldFlags by lazy { resolver.getField(objPath, "flags") }
        private val fieldColor by lazy { resolver.getField(objPath, "color") }
        private val fieldBackgroundEmojiId by lazy { resolver.getField(objPath, "background_emoji_id") }

        var flags: Int
            get() = getIntField(instance, fieldFlags)
            set(value) = setIntField(instance, fieldFlags, value)

        var color: Int
            get() = getIntField(instance, fieldColor)
            set(value) = setIntField(instance, fieldColor, value)

        var backgroundEmojiId: Long
            get() = getLongField(instance, fieldBackgroundEmojiId)
            set(value) = setLongField(instance, fieldBackgroundEmojiId, value)

        fun getNativeInstance() = instance
    }

    class TLPeerColor : PeerColor {
        private val objPath = "org.telegram.tgnet.TLRPC\$TL_peerColor"

        constructor() : super(newInstance(Telegami.loadClass(resolver.get("org.telegram.tgnet.TLRPC\$TL_peerColor"))))

        constructor(instance: Any) : super(instance)
    }

    class TLMessagesAffectedMessages(
        private val instance: Any,
    ) {
        private val objPath = OBJ_PATH

        private val ptsField by lazy { resolver.getField(objPath, "pts") }
        private val ptsCountField by lazy { resolver.getField(objPath, "pts_count") }

        constructor() : this(newInstance(Telegami.loadClass(resolver.get(OBJ_PATH))))

        var pts: Int
            get() = getIntField(instance, ptsField)
            set(value) = setIntField(instance, ptsField, value)

        var ptsCount: Int
            get() = getIntField(instance, ptsCountField)
            set(value) = setIntField(instance, ptsCountField, value)

        fun getNativeInstance() = instance

        companion object {
            const val OBJ_PATH = "org.telegram.tgnet.TLRPC\$TL_messages_affectedMessages"
        }
    }
}
