package com.aoya.telegami.virt.ui.actionbar

import android.content.Context
import com.aoya.telegami.virt.messenger.MessagesController
import com.aoya.telegami.virt.messenger.UserConfig
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

open class BaseFragment(
    protected val instance: Any,
) {
    private val objPath = "org.telegram.ui.ActionBar.BaseFragment"

    fun getContext() =
        instance
            .asResolver()
            .firstMethod {
                name = resolver.getMethod(objPath, "getContext")
                superclass()
            }.invoke()!! as Context

    fun getMessagesController(): MessagesController =
        MessagesController(
            instance
                .asResolver()
                .firstMethod {
                    name = resolver.getMethod(objPath, "getMessagesController")
                    superclass()
                }.invoke()!!,
        )

    fun getUserConfig(): UserConfig =
        UserConfig(
            instance
                .asResolver()
                .firstMethod {
                    name = resolver.getMethod(objPath, "getUserConfig")
                    superclass()
                }.invoke()!!,
        )

    fun presentFragment(fragment: Any) =
        instance
            .asResolver()
            .firstMethod {
                this.name = "presentFragment"
                parameters(*arrayOf(fragment?.javaClass ?: Any::class.java))
            }.invoke(fragment)!! as Boolean
}
