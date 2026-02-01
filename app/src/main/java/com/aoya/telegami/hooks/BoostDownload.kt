package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.messenger.FileLoadOperation

class BoostDownload :
    Hook(
        "boost_download",
        "Boost download speed",
    ) {
    override fun init() {
        if (Telegami.packageName in listOf("it.octogram.android", "tw.nekomimi.nekogram")) return
        findAndHook("org.telegram.messenger.FileLoadOperation", "updateParams", HookStage.AFTER, filter = { true }) { param ->
            val o = FileLoadOperation(param.thisObject())

            val downloadChunkSizeBig = 0x100000 // 1MB

            o.maxDownloadRequests = 4
            o.maxDownloadRequestsBig = 8
            o.downloadChunkSizeBig = downloadChunkSizeBig
            o.maxCdnParts = (0x7D000000L / downloadChunkSizeBig).toInt()
        }
    }
}
