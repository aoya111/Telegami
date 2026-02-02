package com.aoya.telegami.utils

import android.text.SpannableStringBuilder
import android.text.Spanned
import com.aoya.telegami.Telegami
import com.aoya.telegami.data.DeletedMessage
import com.aoya.telegami.virt.messenger.AndroidUtilities
import com.aoya.telegami.virt.messenger.LocaleController
import com.aoya.telegami.virt.messenger.MessageObject
import com.aoya.telegami.virt.ui.components.ColoredImageSpan

object MessageHelper {
    private var spannedStrings: Array<CharSequence?> = arrayOfNulls(4)

    fun createDeletedString(msg: DeletedMessage): CharSequence {
        if (spannedStrings[1] == null) {
            spannedStrings[1] =
                SpannableStringBuilder("\u200B").apply {
                    val drawable =
                        Telegami.context.resources
                            .getIdentifier(
                                "msg_delete",
                                "drawable",
                                Telegami.context.packageName,
                            ).takeIf { it != 0 }
                            ?.let {
                                Telegami.context.getDrawable(it)?.mutate()
                            }
                    val span = ColoredImageSpan.newInstance(drawable)
                    span.setSize(AndroidUtilities.dp(12f))
                    // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    //     span.contentDescription = LocaleController.getString(R.string.EditedMessage)
                    // }
                    setSpan(span.getNativeInstance(), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
        }
        return SpannableStringBuilder().apply {
            append(spannedStrings[1])
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
}
