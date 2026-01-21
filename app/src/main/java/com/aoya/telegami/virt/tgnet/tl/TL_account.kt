package com.aoya.telegami.virt.tgnet.tl

import de.robv.android.xposed.XposedHelpers.getBooleanField
import de.robv.android.xposed.XposedHelpers.setBooleanField
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
            get() = getBooleanField(instance, resolver.getField(objPath, "offline"))
            set(value) = setBooleanField(instance, resolver.getField(objPath, "offline"), value)
    }
}
