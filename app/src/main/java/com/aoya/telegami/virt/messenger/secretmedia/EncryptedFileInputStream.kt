package com.aoya.telegami.virt.messenger.secretmedia

import com.aoya.telegami.Telegami
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import java.io.File
import java.io.InputStream
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class EncryptedFileInputStream(
    private val instance: Any,
) : InputStream() {
    private val objPath = OBJ_PATH

    override fun read(): Int =
        instance
            .asResolver()
            .firstMethod {
                this.name = "read"
                parameters()
            }.invoke()!! as Int

    override fun read(
        b: ByteArray,
        off: Int,
        len: Int,
    ): Int =
        instance
            .asResolver()
            .firstMethod {
                this.name = "read"
                parameters(
                    *arrayOf(
                        b?.javaClass ?: Any::class.java,
                        off?.javaClass ?: Any::class.java,
                        len?.javaClass ?: Any::class.java,
                    ),
                )
            }.invoke(b, off, len)!! as Int

    override fun skip(n: Long): Long =
        instance
            .asResolver()
            .firstMethod {
                this.name = "skip"
                parameters(*arrayOf(n?.javaClass ?: Any::class.java))
            }.invoke(n)!! as Long

    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.secretmedia.EncryptedFileInputStream"

        fun create(
            file: File,
            keyFile: File,
        ): EncryptedFileInputStream =
            EncryptedFileInputStream(
                (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>)
                    .resolve()
                    .firstConstructor {
                        parameters(*arrayOf(file?.javaClass ?: Any::class.java, keyFile?.javaClass ?: Any::class.java))
                    }.create(file, keyFile),
            )
    }
}
