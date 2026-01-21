package com.aoya.telegami.virt.ui.components

import android.view.View
import android.view.ViewGroup
import com.aoya.telegami.Telegami
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.newInstance
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ItemOptions private constructor(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    fun setGravity(gravity: Int): ItemOptions =
        apply {
            callMethod(instance, resolver.getMethod(objPath, "setGravity"), gravity)
        }

    fun add(
        iconResId: Int,
        text: CharSequence,
        onClickListener: Runnable,
    ): ItemOptions =
        apply {
            callMethod(instance, resolver.getMethod(objPath, "add"), iconResId, text, onClickListener)
        }

    fun show(): ItemOptions =
        apply {
            callMethod(instance, resolver.getMethod(objPath, "show"))
        }

    companion object {
        private const val OBJ_PATH = "org.telegram.ui.Components.ItemOptions"

        fun makeOptions(
            fragment: Any,
            scrimView: View,
            swipeback: Boolean = false,
            withoutScrollView: Boolean = true,
            shownFromBottom: Boolean = false,
        ): ItemOptions =
            newInstance(
                Telegami.loadClass(resolver.get(OBJ_PATH)),
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
                Telegami.loadClass(resolver.get(OBJ_PATH)),
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
                Telegami.loadClass(resolver.get(OBJ_PATH)),
                container,
                resourcesProvider,
                scrimView,
                swipeback,
                shownFromBottom,
            )?.let { ItemOptions(it) } ?: error("instantiation failed")
    }
}
