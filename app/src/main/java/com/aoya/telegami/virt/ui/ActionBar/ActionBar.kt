package com.aoya.telegami.virt.ui.actionbar

import android.graphics.drawable.Drawable
import com.aoya.telegami.virt.ui.actionbar.ActionBarMenu
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ActionBar(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.ActionBar.ActionBar"

    private val fieldMenu by lazy {
        instance
            .asResolver()
            .firstField {
                name = resolver.getField(objPath, "menu")
                superclass()
            }
    }

    val menu: ActionBarMenu?
        get() =
            fieldMenu
                .get()
                ?.let { ActionBarMenu(it) }

    fun createMenu(): ActionBarMenu =
        ActionBarMenu(
            instance
                .asResolver()
                .firstMethod {
                    name = resolver.getMethod(objPath, "createMenu")
                    superclass()
                }.invoke()!!,
        )
}
