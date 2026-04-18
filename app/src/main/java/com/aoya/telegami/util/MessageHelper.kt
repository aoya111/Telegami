package com.aoya.telegami.util

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
    private val icons: Map<String, CharSequence> by lazy {
        mapOf(
            "edit" to createIconSpan("msg_edit", 0xFF5FA8D3.toInt()),
            "delete" to createIconSpan("msg_delete", 0xFFFF6B6B.toInt()),
        )
    }

    private val editedLabel: String by lazy {
        getResourceString("EditedMessage")
    }

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
            msgObj.messageOwner?.let {
                append(
                    LocaleController.getInstance().getFormatterDay().format(
                        it.date.toLong() * 1000,
                    ),
                )
            }
        }

    fun replaceWithIcon(text: CharSequence): CharSequence {
        val label = editedLabel
        if (label.isEmpty()) return text

        val start = text.indexOf(label)
        if (start < 0) return text

        val str = text.toString()
        return SpannableStringBuilder().apply {
            append(str.substring(0, start))
            append(icons["edit"])
            append(' ')
            append(str.substring(start + label.length))
        }
    }

    private fun getResourceString(resourceName: String): String {
        val strResId =
            Telegami
                .getResource(
                    resourceName,
                    "string",
                ).takeIf { it != 0 } ?: return ""
        return LocaleController.getString(strResId)
    }

    private fun createIconSpan(
        resourceName: String,
        color: Int? = null,
    ): CharSequence {
        val drawableResId =
            Telegami
                .getResource(
                    resourceName,
                    "drawable",
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
