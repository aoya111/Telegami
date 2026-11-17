package com.aoya.telegami.virt.ui

import com.aoya.telegami.virt.ui.actionbar.BaseFragment
import com.aoya.telegami.virt.ui.actionbar.SimpleTextView
import com.aoya.telegami.Telegami
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ProfileActivity(
    instance: Any,
) : BaseFragment(instance) {
    private val objPath = "org.telegram.ui.ProfileActivity"

    val myProfile: Boolean
        get() = instance.asResolver().firstField { name = resolver.getField(objPath, "myProfile") }.get<Boolean>()!!

    val userId: Long
        get() = instance.asResolver().firstField { name = resolver.getField(objPath, "userId") }.get<Long>()!!

    val chatId: Long
        get() = instance.asResolver().firstField { name = resolver.getField(objPath, "chatId") }.get<Long>()!!

    val topicId: Long
        get() = instance.asResolver().firstField { name = resolver.getField(objPath, "topicId") }.get<Long>()!!

    val usernameRow: Int
        get() = instance.asResolver().firstField { name = resolver.getField(objPath, "usernameRow") }.get<Int>()!!

    val contentView: Any?
        get() =
            if (Telegami.packageName == "org.telegram.messenger") {
                instance.asResolver().firstField {
                    name = "fragmentView"
                    superclass()
                }.get()
            } else {
                instance.asResolver().firstField { name = resolver.getField(objPath, "contentView") }.get()
            }

    val resourcesProvider: Any?
        get() =
            if (Telegami.packageName == "org.telegram.messenger") {
                instance.asResolver().firstField {
                    name = "resourceProvider"
                    superclass()
                }.get()
            } else {
                instance.asResolver().firstField { name = resolver.getField(objPath, "resourcesProvider") }.get()
            }

    fun setStatusText(text: String) {
        val field =
            instance
                .asResolver()
                .firstField {
                    name = resolver.getField(objPath, "onlineTextView")
                }.get()
        when (field) {
            is Array<*> -> field.getOrNull(1)?.let { SimpleTextView(it).setText(text) }
            null -> Unit
            else -> setSingleStatusText(field, text)
        }
    }

    private fun setSingleStatusText(
        view: Any,
        text: String,
    ) {
        // 13.x uses an AnimatedClickableSmallTextView. Its own M(CharSequence,
        // boolean) animates and can trigger profile layout work; the base class
        // still has the plain D(CharSequence, boolean) text setter. Use that
        // when available so the status can be replaced without side effects.
        val plainSetter =
            view.javaClass.superclass?.methods?.firstOrNull {
                it.parameterCount == 2 &&
                    it.parameterTypes[0] == CharSequence::class.java &&
                    it.parameterTypes[1] == Boolean::class.javaPrimitiveType
            }
        if (plainSetter != null) {
            plainSetter.invoke(view, text, false)
            return
        }
        view
            .asResolver()
            .firstMethod {
                parameters(CharSequence::class.java, Boolean::class.javaPrimitiveType!!)
            }.invoke(text, true)
    }
}
