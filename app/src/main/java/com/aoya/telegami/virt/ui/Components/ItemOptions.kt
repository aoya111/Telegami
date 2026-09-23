package com.aoya.telegami.virt.ui.components

import android.view.View
import android.view.ViewGroup
import com.aoya.telegami.Telegami
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.VagueType
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ItemOptions private constructor(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    fun setGravity(gravity: Int): ItemOptions =
        apply {
            if (Telegami.packageName == "tw.nekomimi.nekogram") return@apply
            instance
                .asResolver()
                .firstMethod {
                    name = resolver.getMethod(objPath, "setGravity")
                    parameters(Int::class.javaPrimitiveType!!)
                }.invoke(gravity)
        }

    fun add(
        iconResId: Int,
        text: CharSequence,
        onClickListener: Runnable,
    ): ItemOptions =
        apply {
            if (Telegami.packageName == "tw.nekomimi.nekogram") {
                instance
                    .asResolver()
                    .firstMethod {
                        name = resolver.getMethod(objPath, "add")
                        parameters(
                            Int::class.javaPrimitiveType!!,
                            CharSequence::class.java,
                            Runnable::class.java,
                            Boolean::class.javaPrimitiveType!!,
                        )
                    }.invoke(iconResId, text, onClickListener, false)
            } else {
                instance
                    .asResolver()
                    .firstMethod {
                        name = resolver.getMethod(objPath, "add")
                        parameters(Int::class.javaPrimitiveType!!, CharSequence::class.java, Runnable::class.java)
                    }.invoke(iconResId, text, onClickListener)
            }
        }

    fun show(): ItemOptions =
        apply {
            instance
                .asResolver()
                .firstMethod {
                    name = resolver.getMethod(objPath, "show")
                }.invoke()
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
            (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>)
                .resolve()
                .firstConstructor {
                    parameters(VagueType, VagueType, Boolean::class, Boolean::class, Boolean::class)
                }.create(fragment, scrimView, swipeback, withoutScrollView, shownFromBottom)
                ?.let { ItemOptions(it) }
                ?: error("instantiation failed")

        fun makeOptions(
            fragment: Any,
            scrimView: View,
        ): ItemOptions =
            (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>)
                .resolve()
                .firstConstructor {
                    parameterCount = 3
                }.create(fragment, null, scrimView)
                ?.let { ItemOptions(it) }
                ?: error("instantiation failed")

        fun makeOptions(
            container: ViewGroup,
            resourcesProvider: Any? = null,
            scrimView: View,
            swipeback: Boolean = false,
            shownFromBottom: Boolean = false,
        ): ItemOptions =
            (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>)
                .resolve()
                .firstConstructor {
                    parameters(VagueType, VagueType, VagueType, Boolean::class, Boolean::class, Boolean::class)
                }.create(container, resourcesProvider, scrimView, swipeback, shownFromBottom, false)
                ?.let { ItemOptions(it) }
                ?: error("instantiation failed")
    }
}
