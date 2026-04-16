package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.core.Config
import com.aoya.telegami.virt.messenger.FileLoadOperation
import com.aoya.telegami.virt.messenger.FileLoader
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object BoostDownload : YukiBaseHooker() {
    const val FILE_LOAD_OPERATION_CN = "org.telegram.messenger.FileLoadOperation"

    val fileLoadOperationClass by lazyClass(resolver.get(FILE_LOAD_OPERATION_CN))

    const val BOOST_NONE = 0
    const val BOOST_ON = 1
    const val BOOST_EXTREME = 2
    val EXCLUDED_PACKAGE = listOf("it.octogram.android", "tw.nekomimi.nekogram", "uz.unnarsx.cherrygram")

    override fun onHook() {
        if (Telegami.packageName in EXCLUDED_PACKAGE) return
        fileLoadOperationClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(FILE_LOAD_OPERATION_CN, "updateParams")
            }.hook {
                after {
                    val o = instance?.let { FileLoadOperation(it) } ?: return@after
                    val boostLevel = Config.getFeatureValue("BoostDownload", BOOST_NONE)

                    when (boostLevel) {
                        BOOST_NONE -> {
                            return@after
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
                }
            }
    }
}
