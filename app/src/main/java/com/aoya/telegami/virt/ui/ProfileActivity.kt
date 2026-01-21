package com.aoya.telegami.virt.ui

import com.aoya.telegami.virt.ui.actionbar.BaseFragment
import com.aoya.telegami.virt.ui.actionbar.SimpleTextView
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getBooleanField
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.getObjectField
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class ProfileActivity(
    instance: Any,
) : BaseFragment(instance) {
    private val objPath = "org.telegram.ui.ProfileActivity"

    val myProfile: Boolean
        get() = getBooleanField(instance, resolver.getField(objPath, "myProfile"))

    val userId: Long
        get() = getLongField(instance, resolver.getField(objPath, "userId"))

    val chatId: Long
        get() = getLongField(instance, resolver.getField(objPath, "chatId"))

    val topicId: Long
        get() = getLongField(instance, resolver.getField(objPath, "topicId"))

    val usernameRow: Int
        get() = getIntField(instance, resolver.getField(objPath, "usernameRow"))

    val contentView: Any?
        get() = getObjectField(instance, resolver.getField(objPath, "contentView"))

    val resourcesProvider: Any?
        get() = getObjectField(instance, resolver.getField(objPath, "resourcesProvider"))

    val onlineTextView: Array<SimpleTextView>
        get() {
            val nativeArray = getObjectField(instance, resolver.getField(objPath, "onlineTextView")) as Array<Any>
            return Array(nativeArray.size) { index ->
                nativeArray[index].let { SimpleTextView(it) }
            }
        }
}
