package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.hook
import com.aoya.telegami.virt.messenger.FileLoadOperation
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class BoostDownload :
    Hook(
        "boost_download",
        "Boost download speed",
    ) {
    override fun init() {
        if (Telegami.packageName == "tw.nekomimi.nekogram") return
        findClass(
            "org.telegram.messenger.FileLoadOperation",
        ).hook(resolver.getMethod("org.telegram.messenger.FileLoadOperation", "updateParams"), HookStage.AFTER) { param ->
            val o = FileLoadOperation(param.thisObject())

            val downloadChunkSizeBig = 0x100000 // 1MB

            o.maxDownloadRequests = 4
            o.maxDownloadRequestsBig = 8
            o.downloadChunkSizeBig = downloadChunkSizeBig
            o.maxCdnParts = (0x7D000000L / downloadChunkSizeBig).toInt()
        }
    }
}
