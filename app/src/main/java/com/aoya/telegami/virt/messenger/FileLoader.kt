package com.aoya.telegami.virt.messenger

import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.tgnet.TLRPC
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.getStaticIntField
import java.io.File
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class FileLoader(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    fun getPathToMessage(message: TLRPC.Message): File =
        callMethod(
            instance,
            resolver.getMethod(objPath, "getPathToMessage"),
            message.getNativeInstance(),
        ) as File

    companion object {
        private const val OBJ_PATH = "org.telegram.messenger.FileLoader"

        val MEDIA_DIR_CACHE: Int
            get() =
                if (Telegami.packageName == "tw.nekomimi.nekogram") {
                    4
                } else {
                    getStaticIntField(Telegami.loadClass(resolver.get(OBJ_PATH)), resolver.getField(OBJ_PATH, "MEDIA_DIR_CACHE"))
                }

        fun getInternalCacheDir(): File =
            callStaticMethod(
                Telegami.loadClass(resolver.get(OBJ_PATH)),
                resolver.getMethod(OBJ_PATH, "getInternalCacheDir"),
            ) as File

        fun getDirectory(type: Int): File =
            callStaticMethod(
                Telegami.loadClass(resolver.get(OBJ_PATH)),
                resolver.getMethod(OBJ_PATH, "getDirectory"),
                type,
            ) as File

        fun getInstance(num: Int): FileLoader =
            FileLoader(
                callStaticMethod(
                    Telegami.loadClass(resolver.get(OBJ_PATH)),
                    resolver.getMethod(OBJ_PATH, "getInstance"),
                    num,
                ),
            )
    }
}
