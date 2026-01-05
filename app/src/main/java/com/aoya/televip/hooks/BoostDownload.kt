package com.aoya.televip.hooks

import com.aoya.televip.TeleVip
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import com.aoya.televip.virt.messenger.FileLoadOperation
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class BoostDownload :
    Hook(
        "boost_download",
        "Boost download speed",
    ) {
    override fun init() {
        if (TeleVip.packageName == "tw.nekomimi.nekogram") return
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
