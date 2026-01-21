package com.aoya.telegami.virt.messenger.secretmedia

import com.aoya.telegami.Telegami
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.newInstance
import java.io.File
import java.io.InputStream
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class EncryptedFileInputStream(
    private val instance: Any,
) : InputStream() {
    private val objPath = OBJ_PATH

    override fun read(): Int = callMethod(instance, "read") as Int

    override fun read(
        b: ByteArray,
        off: Int,
        len: Int,
    ): Int = callMethod(instance, "read", b, off, len) as Int

    override fun skip(n: Long): Long = callMethod(instance, "skip", n) as Long

    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.secretmedia.EncryptedFileInputStream"

        fun create(
            file: File,
            keyFile: File,
        ): EncryptedFileInputStream =
            EncryptedFileInputStream(
                newInstance(
                    Telegami.loadClass(resolver.get(OBJ_PATH)),
                    file,
                    keyFile,
                ),
            )
    }
}
