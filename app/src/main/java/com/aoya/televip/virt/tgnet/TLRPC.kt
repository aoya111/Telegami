package com.aoya.televip.virt.tgnet

import com.aoya.televip.TeleVip
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.newInstance
import de.robv.android.xposed.XposedHelpers.setIntField
import de.robv.android.xposed.XposedHelpers.setLongField
import de.robv.android.xposed.XposedHelpers.setObjectField
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class TLRPC {
    class User(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.tgnet.TLRPC\$User"

        val id: Long
            get() = getLongField(instance, resolver.getField(objPath, "id"))

        val username: String
            get() = (getObjectField(instance, resolver.getField(objPath, "username")) as? String) ?: ""

        var phone: String
            get() = (getObjectField(instance, resolver.getField(objPath, "phone")) as? String) ?: ""
            set(value) = setObjectField(instance, resolver.getField(objPath, "phone"), value)

        var flags: Int
            get() = getIntField(instance, resolver.getField(objPath, "flags"))
            set(value) = setIntField(instance, resolver.getField(objPath, "flags"), value)

        var flags2: Int
            get() = getIntField(instance, resolver.getField(objPath, "flags2"))
            set(value) = setIntField(instance, resolver.getField(objPath, "flags2"), value)

        var color: PeerColor?
            get() = getObjectField(instance, resolver.getField(objPath, "color"))?.let { TLPeerColor(it) }
            set(value) = setObjectField(instance, resolver.getField(objPath, "color"), value?.getNativeInstance())

        var profileColor: PeerColor?
            get() = getObjectField(instance, resolver.getField(objPath, "profile_color"))?.let { TLPeerColor(it) }
            set(value) = setObjectField(instance, resolver.getField(objPath, "profile_color"), value?.getNativeInstance())

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

        var flags: Int
            get() = getIntField(instance, resolver.getField(objPath, "flags"))
            set(value) = setIntField(instance, resolver.getField(objPath, "flags"), value)

        var color: Int
            get() = getIntField(instance, resolver.getField(objPath, "color"))
            set(value) = setIntField(instance, resolver.getField(objPath, "color"), value)

        var backgroundEmojiId: Long
            get() = getLongField(instance, resolver.getField(objPath, "background_emoji_id"))
            set(value) = setLongField(instance, resolver.getField(objPath, "background_emoji_id"), value)

        fun getNativeInstance() = instance
    }

    class TLPeerColor : PeerColor {
        private val objPath = "org.telegram.tgnet.TLRPC\$TL_peerColor"

        constructor() : super(newInstance(TeleVip.loadClass(resolver.get("org.telegram.tgnet.TLRPC\$TL_peerColor"))))

        constructor(instance: Any) : super(instance)
    }
}
