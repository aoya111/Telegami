package com.aoya.televip.hooks

import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedHelpers.setIntField
import de.robv.android.xposed.XposedHelpers.setObjectField

class BoostDownload :
    Hook(
        "boost_download",
        "Boost download speed",
    ) {
    override fun init() {
        findClass(
            "org.telegram.messenger.FileLoadOperation",
        ).hook("updateParams", HookStage.AFTER) { param ->
            val o = param.thisObject()

            val maxDownloadRequests = 4
            val maxDownloadRequestsBig = 8
            val downloadChunkSizeBig = 0x100000 // 1MB
            val maxCdnParts = (0x7D000000L / downloadChunkSizeBig).toInt()

            setIntField(o, "downloadChunkSizeBig", downloadChunkSizeBig)
            setObjectField(o, "maxDownloadRequests", maxDownloadRequests)
            setObjectField(o, "maxDownloadRequestsBig", maxDownloadRequestsBig)
            setObjectField(o, "maxCdnParts", maxCdnParts)
        }
    }
}
