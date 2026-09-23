package com.aoya.telegami.virt.tgnet.tl

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class TLAccount(
    private val instance: Any,
) {
    private val objPath = "org.telegram.tgnet.tl.TL_account"

    class UpdateStatus(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.tgnet.tl.TL_account\$updateStatus"

        var offline: Boolean
            get() = instance.asResolver().firstField { this.name = resolver.getField(objPath, "offline") }.get<Boolean>()!!
            set(value) = instance.asResolver().firstField { this.name = resolver.getField(objPath, "offline") }.set(value)
    }
}
