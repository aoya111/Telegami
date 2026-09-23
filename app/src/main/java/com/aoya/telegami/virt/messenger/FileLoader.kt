package com.aoya.telegami.virt.messenger

import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.tgnet.TLRPC
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import java.io.File
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class FileLoader(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    fun getPathToMessage(message: TLRPC.Message): File =
        instance
            .asResolver()
            .firstMethod {
                name = resolver.getMethod(objPath, "getPathToMessage")
                parameterCount = 1
            }.invoke(message.getNativeInstance())!! as File

    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.FileLoader"

        val DEFAULT_MAX_FILE_SIZE = 1024L * 1024L * 2000L

        val MEDIA_DIR_CACHE = 4

        fun getInternalCacheDir(): File =
            if (Telegami.packageName == "xyz.nextalone.nagram") {
                Telegami.context.getCacheDir()
            } else {
                (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>)
                    .resolve()
                    .firstMethod {
                        name = resolver.getMethod(OBJ_PATH, "getInternalCacheDir")
                    }.invoke()!! as File
            }

        fun getDirectory(type: Int): File =
            (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>)
                .resolve()
                .firstMethod {
                    name = resolver.getMethod(OBJ_PATH, "getDirectory")
                }.invoke(type)!! as File

        fun getInstance(num: Int): FileLoader =
            FileLoader(
                (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>)
                    .resolve()
                    .firstMethod {
                        name = resolver.getMethod(OBJ_PATH, "getInstance")
                    }.invoke(num)!!,
            )
    }
}
