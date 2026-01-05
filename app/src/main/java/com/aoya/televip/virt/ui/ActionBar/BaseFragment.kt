package com.aoya.televip.virt.ui.actionbar

import com.aoya.televip.virt.messenger.MessagesController
import com.aoya.televip.virt.messenger.UserConfig
import de.robv.android.xposed.XposedHelpers.callMethod
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

open class BaseFragment(
    protected val instance: Any,
) {
    private val objPath = "org.telegram.ui.ActionBar.BaseFragment"

    fun getMessagesController(): MessagesController =
        MessagesController(callMethod(instance, resolver.getMethod(objPath, "getMessagesController")))

    fun getUserConfig(): UserConfig = UserConfig(callMethod(instance, resolver.getMethod(objPath, "getUserConfig")))

    fun presentFragment(fragment: Any) = callMethod(instance, "presentFragment", fragment) as Boolean
}
