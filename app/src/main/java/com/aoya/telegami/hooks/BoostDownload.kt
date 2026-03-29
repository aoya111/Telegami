package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.core.Config
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.messenger.FileLoadOperation
import com.aoya.telegami.virt.messenger.FileLoader

class BoostDownload : Hook("BoostDownload") {
    companion object {
        const val BOOST_NONE = 0
        const val BOOST_ON = 1
        const val BOOST_EXTREME = 2

        val EXCLUDED_PACKAGE = listOf("it.octogram.android", "tw.nekomimi.nekogram", "uz.unnarsx.cherrygram")
    }

    override fun init() {
        if (Telegami.packageName in EXCLUDED_PACKAGE) return
        findAndHook("org.telegram.messenger.FileLoadOperation", "updateParams", HookStage.AFTER, filter = { true }) { param ->
            val o = FileLoadOperation(param.thisObject())
            val boostLevel = Config.getFeatureValue(hookKey, BOOST_NONE)

            when (boostLevel) {
                BOOST_NONE -> {
                    return@findAndHook
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
