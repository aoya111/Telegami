package com.aoya.telegami.utils

import android.text.SpannableStringBuilder
import android.text.Spanned
import com.aoya.telegami.Telegami
import com.aoya.telegami.data.DeletedMessage
import com.aoya.telegami.virt.messenger.AndroidUtilities
import com.aoya.telegami.virt.messenger.LocaleController
import com.aoya.telegami.virt.messenger.MessageObject
import com.aoya.telegami.virt.ui.actionbar.Theme
import com.aoya.telegami.virt.ui.components.ColoredImageSpan

object MessageHelper {
    private val icons =
        mapOf<String, CharSequence>(
            "edit" to createIconSpan("msg_edit", 0xFF5FA8D3.toInt()),
            "delete" to createIconSpan("msg_delete", 0xFFFF6B6B.toInt()),
        )
    private val labels =
        mapOf(
            "edit" to getResourceString("EditedMessage"),
        )

    fun createDeletedString(msg: DeletedMessage): CharSequence =
        SpannableStringBuilder().apply {
            append(icons["delete"])
            append(' ')
            msg.createdAt?.let {
                append(
                    LocaleController.getInstance().getFormatterDay().format(
                        it.toLong() * 1000,
                    ),
                )
            }
        }

    fun createEditedString(msgObj: MessageObject): CharSequence =
        SpannableStringBuilder().apply {
            append(icons["edit"])
            append(' ')
            append(
                LocaleController.getInstance().getFormatterDay().format(
                    msgObj.messageOwner.date.toLong() * 1000,
                ),
            )
        }

    fun replaceWithIcon(text: CharSequence): CharSequence {
        val str = text.toString()

        var newTxt = SpannableStringBuilder("")
        for (entry in labels.entries.iterator()) {
            val start = text.indexOf(entry.value)
            if (start < 0) continue

            newTxt =
                SpannableStringBuilder().apply {
                    append(str.substring(0, start))
                    append(icons[entry.key])
                    append(' ')
                    append(str.substring(start + entry.value.length))
                }
            break
        }
        return if (newTxt.isEmpty()) text else newTxt
    }

    private fun getResourceString(resourceName: String): String {
        val strResId =
            Telegami.context.resources
                .getIdentifier(
                    resourceName,
                    "string",
                    Telegami.context.packageName,
                ).takeIf { it != 0 } ?: return ""
        return LocaleController.getString(strResId)
    }

    private fun createIconSpan(
        resourceName: String,
        color: Int? = null,
    ): CharSequence {
        val drawableResId =
            Telegami.context.resources
                .getIdentifier(
                    resourceName,
                    "drawable",
                    Telegami.context.packageName,
                ).takeIf { it != 0 } ?: return ""

        val drawable = Telegami.context.getDrawable(drawableResId)?.mutate()
        val span = ColoredImageSpan.newInstance(drawable)
        span.setSize(Theme.chatTimePaint.textSize.toInt())

        color?.let { span.setOverrideColor(it) }

        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        //     span.contentDescription = LocaleController.getString(R.string.EditedMessage)
        // }
        return SpannableStringBuilder("\u200B").apply {
            setSpan(span.getNativeInstance(), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}
