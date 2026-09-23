package com.aoya.telegami.virt.ui

import com.aoya.telegami.virt.ui.actionbar.BaseFragment
import com.aoya.telegami.virt.ui.actionbar.SimpleTextView
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
        get() = instance.asResolver().firstField { name = resolver.getField(objPath, "contentView") }.get()!!

    val resourcesProvider: Any?
        get() = instance.asResolver().firstField { name = resolver.getField(objPath, "resourcesProvider") }.get()

    val onlineTextView: Array<SimpleTextView>
        get() {
            val nativeArray =
                instance
                    .asResolver()
                    .firstField {
                        name =
                            resolver.getField(
                                objPath,
                                "onlineTextView",
                            )
                    }.get()!! as Array<Any>
            return Array(nativeArray.size) { index ->
                nativeArray[index].let { SimpleTextView(it) }
            }
        }
}
