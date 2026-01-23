package com.aoya.telegami.virt.ui

import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.getObjectField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class PeerColorActivity(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.PeerColorActivity"

    val profilePage: Page
        get() = Page(getObjectField(instance, resolver.getField(objPath, "profilePage")))

    val namePage: Page
        get() = Page(getObjectField(instance, resolver.getField(objPath, "namePage")))

    class Page(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.ui.PeerColorActivity\$Page"

        val selectedColor: Int
            get() = getIntField(instance, resolver.getField(objPath, "selectedColor"))

        val selectedEmoji: Long
            get() = getLongField(instance, resolver.getField(objPath, "selectedEmoji"))
    }
}
