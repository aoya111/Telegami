package com.aoya.televip.ui.components

import android.view.View
import android.view.ViewGroup
import com.aoya.televip.TeleVip
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.newInstance

class ItemOptions(
    container: ViewGroup,
    resourcesProvider: Any? = null,
    scrimView: View,
    swipeBack: Boolean? = false,
) {
    private var itemOpts: Any

    init {
        itemOpts =
            newInstance(
                TeleVip.loadClass("org.telegram.ui.Components.ItemOptions"),
                container,
                resourcesProvider,
                scrimView,
                swipeBack,
            )
    }

    fun setGravity(gravity: Int): ItemOptions {
        callMethod(itemOpts, "setGravity", gravity)
        return this
    }

    fun add(
        iconResId: Int,
        text: CharSequence,
        onClickListener: Runnable,
    ): ItemOptions {
        callMethod(itemOpts, "add", iconResId, text, onClickListener)
        return this
    }

    fun show(): ItemOptions {
        callMethod(itemOpts, "show")
        return this
    }
}
