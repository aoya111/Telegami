package com.aoya.televip.hooks

import com.aoya.televip.TeleVip
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedHelpers.setIntField
import de.robv.android.xposed.XposedHelpers.setObjectField
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
            val o = param.thisObject()

            val maxDownloadRequests = 4
            val maxDownloadRequestsBig = 8
            val downloadChunkSizeBig = 0x100000 // 1MB
            val maxCdnParts = (0x7D000000L / downloadChunkSizeBig).toInt()

            setIntField(o, resolver.getField("org.telegram.messenger.FileLoadOperation", "downloadChunkSizeBig"), downloadChunkSizeBig)
            setObjectField(o, resolver.getField("org.telegram.messenger.FileLoadOperation", "maxDownloadRequests"), maxDownloadRequests)
            setObjectField(
                o,
                resolver.getField("org.telegram.messenger.FileLoadOperation", "maxDownloadRequestsBig"),
                maxDownloadRequestsBig,
            )
            setObjectField(o, resolver.getField("org.telegram.messenger.FileLoadOperation", "maxCdnParts"), maxCdnParts)
        }
    }
}
