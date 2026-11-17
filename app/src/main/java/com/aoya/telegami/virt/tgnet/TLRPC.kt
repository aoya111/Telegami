package com.aoya.telegami.virt.tgnet

import com.aoya.telegami.Telegami
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class TLRPC {
    companion object {
        const val MESSAGE_FLAG_EDITED = 0x00008000
    }

    class User(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.tgnet.TLRPC\$User"

        private val fieldId by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "id")
                    superclass()
                }
        }
        private val fieldUsername by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "username")
                    superclass()
                }
        }
        private val fieldFlags by lazy {
            instance.asResolver().firstField {
                name = resolver.getField(objPath, "flags")
                superclass()
            }
        }
        private val fieldFlags2 by lazy {
            instance.asResolver().firstField {
                name = resolver.getField(objPath, "flags2")
                superclass()
            }
        }
        private val fieldColor by lazy {
            instance.asResolver().firstField {
                name = resolver.getField(objPath, "color")
                superclass()
            }
        }
        private val fieldProfileColor by lazy {
            instance.asResolver().firstField {
                name = resolver.getField(objPath, "profile_color")
                superclass()
            }
        }

        val id: Long
            get() = fieldId.get<Long>()!!

        val username: String
            get() = fieldUsername.get()!! as? String ?: ""

        private val fieldPremium by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "premium")
                    superclass()
                }
        }

        var premium: Boolean
            get() = fieldPremium.get<Boolean>()!!
            set(value) = fieldPremium.set(value)

        var flags: Int
            get() = fieldFlags.get<Int>()!!
            set(value) = fieldFlags.set(value)

        var flags2: Int
            get() = fieldFlags.get<Int>()!!
            set(value) = fieldFlags.set(value)

        var color: PeerColor?
            get() =
                fieldColor
                    .get()
                    ?.let { TLPeerColor(it) }
            set(value) = fieldColor.set(value?.getNativeInstance())

        var profileColor: PeerColor?
            get() =
                fieldProfileColor
                    .get()
                    ?.let { TLPeerColor(it) }
            set(value) = fieldProfileColor.set(value?.getNativeInstance())

        fun getNativeInstance() = instance
    }

    class Message(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.tgnet.TLRPC\$Message"

        private val fieldDate by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "date")
                    superclass()
                }
        }
        private val fieldFlags by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "flags")
                    superclass()
                }
        }
        private val fieldTtl by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "ttl")
                    superclass()
                }
        }
        private val fieldMedia by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "media")
                    superclass()
                }
        }
        private val fieldEditDate by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "edit_date")
                    superclass()
                }
        }

        val date: Int
            get() = fieldDate.get<Int>()!!

        val flags: Int
            get() = fieldFlags.get<Int>()!!

        val editDate: Int
            get() = runCatching { fieldEditDate.get<Int>()!! }.getOrDefault(0)

        var ttl: Int
            get() = fieldTtl.get<Int>()!!
            set(value) = fieldTtl.set(value)

        val media: MessageMedia
            get() = MessageMedia(fieldMedia.get()!!)

        fun getNativeInstance() = instance
    }

    class MessageMedia(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.tgnet.TLRPC\$MessageMedia"

        private val fieldTtl by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "ttl")
                    superclass()
                }
        }
        private val fieldMedia by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "media")
                    superclass()
                }
        }

        var ttl: Int
            get() = fieldTtl.get<Int>()!!
            set(value) = fieldTtl.set(value)

        val media: Any?
            get() = fieldMedia.get()!!

        fun getNativeInstance() = instance
    }

    class Chat(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.tgnet.TLRPC\$Chat"

        private val fieldUsername by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "username")
                    superclass()
                }
        }

        val username: String
            get() = fieldUsername.get() as? String ?: ""

        private val fieldNoForwards by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "noforwards")
                    superclass()
                }
        }

        var noforwards: Boolean
            get() = fieldNoForwards.get<Boolean>()!!
            set(value) = fieldNoForwards.set(value)

        fun getNativeInstance() = instance
    }

    abstract class PeerColor(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.tgnet.TLRPC\$PeerColor"

        private val fieldFlags by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "flags")
                    superclass()
                }
        }
        private val fieldColor by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "color")
                    superclass()
                }
        }
        private val fieldBackgroundEmojiId by lazy {
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "background_emoji_id")
                    superclass()
                }
        }

        var flags: Int
            get() = fieldFlags.get<Int>()!!
            set(value) = fieldFlags.set(value)

        var color: Int
            get() = fieldColor.get<Int>()!!
            set(value) = fieldColor.set(value)

        var backgroundEmojiId: Long
            get() = fieldBackgroundEmojiId.get<Long>()!!
            set(value) = fieldBackgroundEmojiId.set(value)

        fun getNativeInstance() = instance
    }

    class TLPeerColor : PeerColor {
        private val objPath = "org.telegram.tgnet.TLRPC\$TL_peerColor"

        constructor() : super(
            (
                Telegami.loadClass(
                    resolver.get("org.telegram.tgnet.TLRPC\$TL_peerColor"),
                ) as Class<Any>
            ).resolve()
                .firstConstructor {
                    parameters()
                }.create(),
        )

        constructor(instance: Any) : super(instance)

        companion object {
            fun allocate(): TLPeerColor? =
                runCatching {
                    val clazz = Telegami.loadClass(resolver.get("org.telegram.tgnet.TLRPC\$TL_peerColor"))
                    val unsafeClass = Class.forName("sun.misc.Unsafe")
                    val theUnsafe =
                        unsafeClass.getDeclaredField("theUnsafe").apply { isAccessible = true }.get(null)
                    val instance =
                        unsafeClass
                            .getMethod("allocateInstance", Class::class.java)
                            .invoke(theUnsafe, clazz)
                    TLPeerColor(instance)
                }.getOrNull()
        }
    }

    class TLBoolTrue(
        private val instance: Any,
    ) {
        fun getNativeInstance() = instance

        companion object {
            private const val OBJ_PATH = "org.telegram.tgnet.TLRPC\$TL_boolTrue"

            fun allocate(): TLBoolTrue? =
                runCatching {
                    val clazz = Telegami.loadClass(resolver.get(OBJ_PATH))
                    val unsafeClass = Class.forName("sun.misc.Unsafe")
                    val theUnsafe =
                        unsafeClass.getDeclaredField("theUnsafe").apply { isAccessible = true }.get(null)
                    val instance =
                        unsafeClass
                            .getMethod("allocateInstance", Class::class.java)
                            .invoke(theUnsafe, clazz)
                    TLBoolTrue(instance)
                }.getOrNull()
        }
    }

    class TLMessagesAffectedMessages(
        private val instance: Any,
    ) {
        private val objPath = OBJ_PATH

        private val ptsField by lazy { resolver.getField(objPath, "pts") }
        private val ptsCountField by lazy { resolver.getField(objPath, "pts_count") }

        constructor() : this(
            (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>).resolve().firstConstructor { parameters() }.create(),
        )

        var pts: Int
            get() = instance.asResolver().firstField { this.name = ptsField }.get<Int>()!!
            set(value) = instance.asResolver().firstField { this.name = ptsField }.set(value)

        var ptsCount: Int
            get() = instance.asResolver().firstField { this.name = ptsCountField }.get<Int>()!!
            set(value) = instance.asResolver().firstField { this.name = ptsCountField }.set(value)

        fun getNativeInstance() = instance

        companion object {
            const val OBJ_PATH = "org.telegram.tgnet.TLRPC\$TL_messages_affectedMessages"

            /**
             * Allocate without invoking a constructor. R8 may remove the
             * no-arg constructor from TL classes in newer builds.
             */
            fun allocate(): TLMessagesAffectedMessages? =
                runCatching {
                    val clazz = Telegami.loadClass(resolver.get(OBJ_PATH))
                    val unsafeClass = Class.forName("sun.misc.Unsafe")
                    val theUnsafe =
                        unsafeClass.getDeclaredField("theUnsafe").apply { isAccessible = true }.get(null)
                    val instance =
                        unsafeClass
                            .getMethod("allocateInstance", Class::class.java)
                            .invoke(theUnsafe, clazz)
                    TLMessagesAffectedMessages(instance)
                }.getOrNull()
        }
    }
}
