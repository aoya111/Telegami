package com.aoya.televip.ui.components

import android.view.View
import android.view.ViewGroup
import com.aoya.televip.TeleVip
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.newInstance
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class ItemOptions private constructor(
    private val itemOpts: Any,
) {
    fun setGravity(gravity: Int): ItemOptions =
        apply {
            callMethod(itemOpts, resolver.getMethod("org.telegram.ui.Components.ItemOptions", "setGravity"), gravity)
        }

    fun add(
        iconResId: Int,
        text: CharSequence,
        onClickListener: Runnable,
    ): ItemOptions =
        apply {
            callMethod(itemOpts, resolver.getMethod("org.telegram.ui.Components.ItemOptions", "add"), iconResId, text, onClickListener)
        }

    fun show(): ItemOptions =
        apply {
            callMethod(itemOpts, resolver.getMethod("org.telegram.ui.Components.ItemOptions", "show"))
        }

    companion object {
        fun makeOptions(
            fragment: Any,
            scrimView: View,
            swipeback: Boolean = false,
            withoutScrollView: Boolean = true,
            shownFromBottom: Boolean = false,
        ): ItemOptions =
            newInstance(
                TeleVip.loadClass(resolver.get("org.telegram.ui.Components.ItemOptions")),
                fragment,
                scrimView,
                swipeback,
                withoutScrollView,
                shownFromBottom,
            )?.let { ItemOptions(it) } ?: error("instantiation failed")

        fun makeOptions(
            fragment: Any,
            scrimView: View,
        ): ItemOptions =
            newInstance(
                TeleVip.loadClass(resolver.get("org.telegram.ui.Components.ItemOptions")),
                fragment,
                null,
                scrimView,
            )?.let { ItemOptions(it) } ?: error("instantiation failed")

        fun makeOptions(
            container: ViewGroup,
            resourcesProvider: Any? = null,
            scrimView: View,
            swipeback: Boolean = false,
            shownFromBottom: Boolean = false,
        ): ItemOptions =
            newInstance(
                TeleVip.loadClass(resolver.get("org.telegram.ui.Components.ItemOptions")),
                container,
                resourcesProvider,
                scrimView,
                swipeback,
                shownFromBottom,
            )?.let { ItemOptions(it) } ?: error("instantiation failed")
    }
}
