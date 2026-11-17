package com.aoya.telegami.hooks

import android.util.Log
import com.aoya.telegami.Telegami
import com.aoya.telegami.service.Config
import com.aoya.telegami.util.findMethod
import com.aoya.telegami.util.logw
import com.aoya.telegami.virt.messenger.FileLoadOperation
import com.aoya.telegami.virt.messenger.FileLoader
import io.github.libxposed.api.XposedInterface

object BoostDownload {
    const val FILE_LOAD_OPERATION_CN = "org.telegram.messenger.FileLoadOperation"

    const val BOOST_NONE = 0
    const val BOOST_ON = 1
    const val BOOST_EXTREME = 2
    val EXCLUDED_PACKAGE = listOf("it.octogram.android", "tw.nekomimi.nekogram", "uz.unnarsx.cherrygram")

    fun install(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        if (Telegami.packageName in EXCLUDED_PACKAGE) return
        val method =
            runCatching { classLoader.findMethod(FILE_LOAD_OPERATION_CN, "updateParams") }.getOrNull()
        if (method == null) {
            logw("BoostDownload skipped: FileLoadOperation.updateParams is not mapped")
            return
        }
        xposed.hook(method).intercept { chain ->
            val args = chain.args.toMutableList()
            val result = chain.proceed(args.toTypedArray())
            try {
                val o = chain.thisObject?.let { FileLoadOperation(it) } ?: return@intercept result
                val boostLevel = Config.getFeatureValue("BoostDownload", BOOST_NONE)

                when (boostLevel) {
                    BOOST_NONE -> {
                        return@intercept result
                    }

                    BOOST_ON -> {
                        o.maxDownloadRequests = 8
                        o.maxDownloadRequestsBig = 8
                        o.downloadChunkSizeBig = 1024 * 512
                    }

                    BOOST_EXTREME -> {
                        o.maxDownloadRequests = 12
                        o.maxDownloadRequestsBig = 12
                        o.downloadChunkSizeBig = 1024 * 1024
                    }
                }

                o.maxCdnParts = (FileLoader.DEFAULT_MAX_FILE_SIZE / o.downloadChunkSizeBig).toInt()
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed after ${method.name}", throwable)
            }
            result
        }
    }
}
