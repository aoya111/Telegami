package com.aoya.telegami.hooks

import com.aoya.telegami.Telegami
import com.aoya.telegami.service.Config
import com.aoya.telegami.virt.messenger.LocaleController
import com.aoya.telegami.virt.messenger.MessageObject
import com.aoya.telegami.util.findMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver
import io.github.libxposed.api.XposedInterface
import android.util.Log

object ModifyDeletedMessagesMenu {
    const val CHAT_ACTIVITY_CN = "org.telegram.ui.ChatActivity"
    fun install(xposed: XposedInterface, classLoader: ClassLoader) {
        val method = classLoader.findMethod(CHAT_ACTIVITY_CN, "fillMessageMenu")
        xposed.hook(method).intercept { chain ->
            val args = chain.args.toMutableList()
            var result: Any? = null
            var hasResult = false
            try {
                    val msgObj = args[0]?.let { MessageObject(it) } ?: return@intercept chain.proceed(args.toTypedArray())
                    val arrayList = args[1] as? ArrayList<Int> ?: return@intercept chain.proceed(args.toTypedArray())
                    val arrayList2 = args[2] as? ArrayList<String> ?: return@intercept chain.proceed(args.toTypedArray())
                    val arrayList3 = args[3] as? ArrayList<Int> ?: return@intercept chain.proceed(args.toTypedArray())

                    if (Globals.isDeletedMessage(msgObj.getDialogId(), msgObj.getId())) {
                        arrayList2.add(LocaleController.getString(Telegami.getResource("Copy", "string")))
                        arrayList2.add(LocaleController.getString(Telegami.getResource("Delete", "string")))
                        arrayList3.add(3)
                        arrayList3.add(1)
                        arrayList.add(Telegami.getResource("msg_copy", "drawable"))
                        arrayList.add(Telegami.getResource("msg_delete", "drawable"))
                        hasResult = true
                    }
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed before ${method.name}", throwable)
            }
            if (!hasResult) result = chain.proceed(args.toTypedArray())
            result
        }
    }
}
