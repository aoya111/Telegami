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
    private var editIcon: CharSequence? = null
    private var deleteIcon: CharSequence? = null

    fun createDeletedString(msg: DeletedMessage): CharSequence {
        if (deleteIcon == null) {
            deleteIcon = createIconSpan("msg_delete", color = 0xFFFF6B6B.toInt())
        }
        return SpannableStringBuilder().apply {
            append(deleteIcon)
            append(' ')
            msg.createdAt?.let {
                append(
                    LocaleController.getInstance().getFormatterDay().format(
                        it.toLong() * 1000,
                    ),
                )
            }
        }
    }

    fun createEditedString(msgObj: MessageObject): CharSequence {
        if (editIcon == null) {
            editIcon = createIconSpan("msg_edit", color = 0xFF5FA8D3.toInt())
        }
        return SpannableStringBuilder().apply {
            append(editIcon)
            append(' ')
            append(
                LocaleController.getInstance().getFormatterDay().format(
                    msgObj.messageOwner.date.toLong() * 1000,
                ),
            )
        }
    }

    fun replaceWithIcon(text: CharSequence): CharSequence {
        if (editIcon == null) {
            editIcon = createIconSpan("msg_edit", color = 0xFF5FA8D3.toInt())
        }

        val str = text.toString()

        var newTxt = SpannableStringBuilder("")
        for (l in listOf("EditedMessage")) {
            val editedStr = getResourceString(l)
            val start = text.indexOf(editedStr)
            if (start < 0) continue

            newTxt =
                SpannableStringBuilder().apply {
                    append(str.substring(0, start))
                    append(editIcon)
                    append(' ')
                    append(str.substring(start + editedStr.length))
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
