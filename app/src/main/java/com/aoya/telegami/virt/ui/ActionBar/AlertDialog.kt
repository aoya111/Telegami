package com.aoya.telegami.virt.ui.actionbar

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import android.content.Context
import android.content.DialogInterface
import android.widget.LinearLayout
import com.aoya.telegami.Telegami
import java.lang.reflect.Proxy
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

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
                (Telegami.loadClass("$alertDialogName\$Builder") as Class<Any>).resolve().firstConstructor { parameters(*arrayOf(ctx?.javaClass ?: Any::class.java, resourcesProvider?.javaClass ?: Any::class.java)) }.create(ctx, resourcesProvider)

            onClickListenerClass = Telegami.loadClass(resolver.get("$alertDialogName\$OnButtonClickListener"))
        }

        fun setTitle(title: String): Builder {
            alertDialog.asResolver().firstMethod { this.name = resolver.getMethod(alertDialogName, "setTitle"); parameters(*arrayOf(title?.javaClass ?: Any::class.java)) }.invoke(title)!!
            return this
        }

        fun setItems(
            items: Array<CharSequence>,
            onClickListener: DialogInterface.OnClickListener,
        ): Builder {
            alertDialog.asResolver().firstMethod { this.name = resolver.getMethod(alertDialogName, "setItems"); parameters(*arrayOf(items?.javaClass ?: Any::class.java, onClickListener?.javaClass ?: Any::class.java)) }.invoke(items, onClickListener)!!
            return this
        }

        fun setView(layout: LinearLayout): Builder {
            alertDialog.asResolver().firstMethod { this.name = resolver.getMethod(alertDialogName, "setView"); parameters(*arrayOf(layout?.javaClass ?: Any::class.java)) }.invoke(layout)!!
            return this
        }

        fun setPositiveButton(
            text: String,
            onClick: (dialog: DialogBuilder) -> Unit,
        ): Builder {
            alertDialog.asResolver().firstMethod { this.name = resolver.getMethod(alertDialogName, "setPositiveButton"); parameters(*arrayOf(text?.javaClass ?: Any::class.java, Proxy.newProxyInstance(
                    ctx.classLoader,
                    arrayOf(onClickListenerClass),
                ) { _, method, args ->
                    if (method.name == resolver.getMethod("$alertDialogName\$OnButtonClickListener", "onClick")) {
                        val dialog = args?.getOrNull(0)
                        if (dialog != null) onClick(DialogBuilder(dialog))
                    }
                    null
                }?.javaClass ?: Any::class.java)) }.invoke(text, Proxy.newProxyInstance(
                    ctx.classLoader,
                    arrayOf(onClickListenerClass),
                ) { _, method, args ->
                    if (method.name == resolver.getMethod("$alertDialogName\$OnButtonClickListener", "onClick")) {
                        val dialog = args?.getOrNull(0)
                        if (dialog != null) onClick(DialogBuilder(dialog))
                    }
                    null
                })!!
            return this
        }

        fun setNegativeButton(
            text: String,
            onClick: (dialog: DialogBuilder) -> Unit,
        ): Builder {
            alertDialog.asResolver().firstMethod { this.name = resolver.getMethod(alertDialogName, "setNegativeButton"); parameters(*arrayOf(text?.javaClass ?: Any::class.java, Proxy.newProxyInstance(
                    ctx.classLoader,
                    arrayOf(onClickListenerClass),
                ) { _, method, args ->
                    if (method.name == resolver.getMethod("$alertDialogName\$OnButtonClickListener", "onClick")) {
                        val dialog = args?.getOrNull(0)
                        if (dialog != null) onClick(DialogBuilder(dialog))
                    }
                    null
                }?.javaClass ?: Any::class.java)) }.invoke(text, Proxy.newProxyInstance(
                    ctx.classLoader,
                    arrayOf(onClickListenerClass),
                ) { _, method, args ->
                    if (method.name == resolver.getMethod("$alertDialogName\$OnButtonClickListener", "onClick")) {
                        val dialog = args?.getOrNull(0)
                        if (dialog != null) onClick(DialogBuilder(dialog))
                    }
                    null
                })!!
            return this
        }

        fun show() {
            alertDialog.asResolver().firstMethod { this.name = resolver.getMethod(alertDialogName, "show"); parameters() }.invoke()!!
        }
    }
}

class DialogBuilder(
    private val dialogInstance: Any,
) {
    fun dismiss() {
        dialogInstance.asResolver().firstMethod { this.name = "dismiss"; parameters() }.invoke()!!
    }
}
