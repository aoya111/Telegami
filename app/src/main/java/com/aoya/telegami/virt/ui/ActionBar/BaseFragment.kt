package com.aoya.telegami.virt.ui.actionbar

import android.content.Context
import com.aoya.telegami.virt.messenger.MessagesController
import com.aoya.telegami.virt.messenger.UserConfig
import de.robv.android.xposed.XposedHelpers.callMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

open class BaseFragment(
    protected val instance: Any,
) {
    private val objPath = "org.telegram.ui.ActionBar.BaseFragment"

    fun getContext() = callMethod(instance, resolver.getMethod(objPath, "getContext")) as Context

    fun getMessagesController(): MessagesController =
        MessagesController(callMethod(instance, resolver.getMethod(objPath, "getMessagesController")))

    fun getUserConfig(): UserConfig = UserConfig(callMethod(instance, resolver.getMethod(objPath, "getUserConfig")))

    fun presentFragment(fragment: Any) = callMethod(instance, "presentFragment", fragment) as Boolean
}
