package com.aoya.televip.hooks

import android.text.SpannableStringBuilder
import com.aoya.televip.TeleVip
import com.aoya.televip.utils.Hook
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.getStaticObjectField
import de.robv.android.xposed.XposedHelpers.setIntField
import de.robv.android.xposed.XposedHelpers.setObjectField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.ceil
import com.aoya.televip.core.i18n.TranslationManager as i18n
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class MarkDeletedMessages :
    Hook(
        "mark_deleted_messages",
        "Mark 'Deleted' messages",
    ) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun init() {
        findClass(
            "org.telegram.ui.Cells.ChatMessageCell",
        ).hook(resolver.getMethod("org.telegram.ui.Cells.ChatMessageCell", "measureTime"), HookStage.AFTER) { param ->
            val o = param.thisObject()
            val msgObj = param.arg<Any>(0)
            val dialogId = callMethod(msgObj, resolver.getMethod("org.telegram.messenger.MessageObject", "getDialogId")) as Long
            try {
                runBlocking {
                    launch {
                        val msgs = db.deletedMessageDao().getAllForDialog(dialogId)
                        val mid = callMethod(msgObj, resolver.getMethod("org.telegram.messenger.MessageObject", "getId")) as Int

                        msgs.find { it.id == mid }?.let { msg ->
                            val delMsgStr = i18n.get("DeletedMessage")

                            var timeStr = delMsgStr
                            msg.createdAt?.let {
                                val dayFormatter =
                                    callMethod(
                                        callStaticMethod(
                                            findClass("org.telegram.messenger.LocaleController"),
                                            resolver.getMethod("org.telegram.messenger.LocaleController", "getInstance"),
                                        ),
                                        resolver.getMethod("org.telegram.messenger.LocaleController", "getFormatterDay"),
                                    )
                                timeStr +=
                                    " " +
                                    callMethod(
                                        dayFormatter,
                                        resolver.getMethod("org.telegram.messenger.time.FastDateFormat", "format"),
                                        it * 1000L,
                                    ) as String
                            }
                            val timeTextWidth =
                                ceil(
                                    callMethod(
                                        getStaticObjectField(
                                            findClass("org.telegram.ui.ActionBar.Theme"),
                                            resolver.getField("org.telegram.ui.ActionBar.Theme", "chat_timePaint"),
                                        ),
                                        "measureText",
                                        timeStr,
                                        0,
                                        timeStr.length,
                                    ) as Float,
                                ).toInt()
                            var timeWidth = timeTextWidth
                            if (TeleVip.packageName == "tw.nekomimi.nekogram") {
                                setObjectField(o, "currentTimeString", SpannableStringBuilder(timeStr))
                            } else {
                                setObjectField(o, "currentTimeString", timeStr)
                            }
                            setIntField(o, "timeTextWidth", timeTextWidth)
                            setIntField(o, "timeWidth", timeWidth)
                        }
                    }.join()
                }
            } catch (e: Exception) {
                XposedBridge.log("Error parsing messages: ${e.message}")
            }
        }
    }
}
