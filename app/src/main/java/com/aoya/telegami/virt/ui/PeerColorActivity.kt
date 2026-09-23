package com.aoya.telegami.virt.ui

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class PeerColorActivity(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.PeerColorActivity"

    private val fieldProfilePage by lazy {
        instance
            .asResolver()
            .firstField {
                name = resolver.getField(objPath, "profilePage")
            }
    }
    private val fieldNamePage by lazy {
        instance
            .asResolver()
            .firstField {
                name = resolver.getField(objPath, "namePage")
            }
    }

    val profilePage: Page
        get() = Page(fieldProfilePage.get()!!)

    val namePage: Page
        get() = Page(fieldNamePage.get()!!)

    class Page(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.ui.PeerColorActivity\$Page"

        val selectedColor: Int
            get() = instance.asResolver().firstField { name = resolver.getField(objPath, "selectedColor") }.get<Int>()!!

        val selectedEmoji: Long
            get() = instance.asResolver().firstField { name = resolver.getField(objPath, "selectedEmoji") }.get<Long>()!!
    }
}
