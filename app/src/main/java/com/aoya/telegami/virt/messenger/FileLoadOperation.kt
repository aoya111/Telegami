package com.aoya.telegami.virt.messenger

import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.setIntField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class FileLoadOperation(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.FileLoadOperation"

    var downloadChunkSizeBig: Int
        get() = getIntField(instance, resolver.getField(objPath, "downloadChunkSizeBig"))
        set(value) = setIntField(instance, resolver.getField(objPath, "downloadChunkSizeBig"), value)

    var maxDownloadRequests: Int
        get() = getIntField(instance, resolver.getField(objPath, "maxDownloadRequests"))
        set(value) = setIntField(instance, resolver.getField(objPath, "maxDownloadRequests"), value)

    var maxDownloadRequestsBig: Int
        get() = getIntField(instance, resolver.getField(objPath, "maxDownloadRequestsBig"))
        set(value) = setIntField(instance, resolver.getField(objPath, "maxDownloadRequestsBig"), value)

    var maxCdnParts: Int
        get() = getIntField(instance, resolver.getField(objPath, "maxCdnParts"))
        set(value) = setIntField(instance, resolver.getField(objPath, "maxCdnParts"), value)
}
