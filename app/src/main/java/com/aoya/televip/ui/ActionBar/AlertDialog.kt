package com.aoya.televip.ui.actionbar

import android.content.Context
import android.content.DialogInterface
import android.widget.LinearLayout
import com.aoya.televip.TeleVip
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.newInstance
import java.lang.reflect.Proxy
import com.aoya.televip.core.obfuscate.ResolverManager as resolver

class AlertDialog {
    class Builder(
        val ctx: Context,
        val resourcesProvider: Any? = null,
    ) {
        private val alertDialogName = "org.telegram.ui.ActionBar.AlertDialog"
        private var alertDialog: Any
        private var onClickListenerClass: Class<*>

        init {
            alertDialog =
                newInstance(
                    TeleVip.loadClass("$alertDialogName\$Builder"),
                    ctx,
                    resourcesProvider,
                )

            onClickListenerClass = TeleVip.loadClass(resolver.get("$alertDialogName\$OnButtonClickListener"))
        }

        fun setTitle(title: String): Builder {
            callMethod(alertDialog, resolver.getMethod(alertDialogName, "setTitle"), title)
            return this
        }

        fun setItems(
            items: Array<CharSequence>,
            onClickListener: DialogInterface.OnClickListener,
        ): Builder {
            callMethod(alertDialog, resolver.getMethod(alertDialogName, "setItems"), items, onClickListener)
            return this
        }

        fun setView(layout: LinearLayout): Builder {
            callMethod(alertDialog, resolver.getMethod(alertDialogName, "setView"), layout)
            return this
        }

        fun setPositiveButton(
            text: String,
            onClick: (dialog: DialogBuilder) -> Unit,
        ): Builder {
            callMethod(
                alertDialog,
                resolver.getMethod(alertDialogName, "setPositiveButton"),
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
        ): Builder {
            callMethod(
                alertDialog,
                resolver.getMethod(alertDialogName, "setNegativeButton"),
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
            callMethod(alertDialog, resolver.getMethod(alertDialogName, "show"))
        }
    }
}

class DialogBuilder(
    private val dialogInstance: Any,
) {
    fun dismiss() {
        callMethod(dialogInstance, "dismiss")
    }
}
