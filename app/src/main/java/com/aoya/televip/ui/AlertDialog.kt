package com.aoya.televip.ui

import android.content.Context
import android.widget.LinearLayout
import com.aoya.televip.TeleVip
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.newInstance
import java.lang.reflect.Proxy

class AlertDialogBuilder(
    val ctx: Context,
    val resourcesProvider: Any? = null,
) {
    private var alertDialog: Any
    private var onClickListenerClass: Class<*>

    init {
        alertDialog =
            newInstance(
                TeleVip.loadClass("org.telegram.ui.ActionBar.AlertDialog\$Builder"),
                ctx,
                resourcesProvider,
            )

        onClickListenerClass = TeleVip.loadClass("org.telegram.ui.ActionBar.AlertDialog\$OnButtonClickListener")
    }

    fun setTitle(title: String): AlertDialogBuilder {
        callMethod(alertDialog, "setTitle", title)
        return this
    }

    fun setView(layout: LinearLayout): AlertDialogBuilder {
        callMethod(alertDialog, "setView", layout)
        return this
    }

    fun setPositiveButton(
        text: String,
        onClick: (dialog: DialogBuilder) -> Unit,
    ): AlertDialogBuilder {
        callMethod(
            alertDialog,
            "setPositiveButton",
            text,
            Proxy.newProxyInstance(
                ctx.classLoader,
                arrayOf(onClickListenerClass),
            ) { _, method, args ->
                if (method.name == "onClick") {
                    val dialog = args?.getOrNull(0)
                    if (dialog != null) onClick(DialogBuilder(dialog))
                }
                null
            },
        )
        return this
    }

    fun setNegativeButton(
        text: String,
        onClick: (dialog: DialogBuilder) -> Unit,
    ): AlertDialogBuilder {
        callMethod(
            alertDialog,
            "setNegativeButton",
            text,
            Proxy.newProxyInstance(
                ctx.classLoader,
                arrayOf(onClickListenerClass),
            ) { _, method, args ->
                if (method.name == "onClick") {
                    val dialog = args?.getOrNull(0)
                    if (dialog != null) onClick(DialogBuilder(dialog))
                }
                null
            },
        )
        return this
    }

    fun show() {
        callMethod(alertDialog, "show")
    }
}

class DialogBuilder(
    private val dialogInstance: Any,
) {
    fun dismiss() {
        callMethod(dialogInstance, "dismiss")
    }
}
