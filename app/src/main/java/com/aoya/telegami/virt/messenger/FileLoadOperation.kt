package com.aoya.telegami.virt.messenger

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class FileLoadOperation(
    private val instance: Any,
) {
    private val objPath = "org.telegram.messenger.FileLoadOperation"

    private val fieldDownloadChunkSizeBig by lazy {
        instance
            .asResolver()
            .firstField {
                name = resolver.getField(objPath, "downloadChunkSizeBigcolor")
            }
    }
    private val fieldMaxDownloadRequests by lazy {
        instance
            .asResolver()
            .firstField {
                name = resolver.getField(objPath, "maxDownloadRequests")
            }
    }
    private val fieldMaxDownloadRequestsBig by lazy {
        instance
            .asResolver()
            .firstField {
                name = resolver.getField(objPath, "maxDownloadRequestsBig")
            }
    }
    private val fieldMaxCdnParts by lazy {
        instance
            .asResolver()
            .firstField {
                name = resolver.getField(objPath, "maxCdnParts")
            }
    }

    var downloadChunkSizeBig: Int
        get() = fieldDownloadChunkSizeBig.get<Int>()!!
        set(value) = fieldDownloadChunkSizeBig.set(value)

    var maxDownloadRequests: Int
        get() = fieldMaxDownloadRequests.get<Int>()!!
        set(value) = fieldMaxDownloadRequests.set(value)

    var maxDownloadRequestsBig: Int
        get() = fieldMaxDownloadRequestsBig.get<Int>()!!
        set(value) = fieldMaxDownloadRequestsBig.set(value)

    var maxCdnParts: Int
        get() = fieldMaxCdnParts.get<Int>()!!
        set(value) = fieldMaxCdnParts.set(value)
}
