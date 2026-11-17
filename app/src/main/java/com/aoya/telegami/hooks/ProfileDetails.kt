package com.aoya.telegami.hooks

import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.aoya.telegami.Telegami
import com.aoya.telegami.util.findMethod
import com.aoya.telegami.virt.messenger.AndroidUtilities
import com.aoya.telegami.virt.ui.ProfileActivity
import com.aoya.telegami.virt.ui.components.ItemOptions
import io.github.libxposed.api.XposedInterface
import com.aoya.telegami.core.i18n.TranslationManager as i18n

object ProfileDetails {
    const val PROFILE_ACTIVITY_CN = "org.telegram.ui.ProfileActivity"
    const val ITEM_OPTIONS_CN = "org.telegram.ui.Components.ItemOptions"

    private data class CopyIdItem(
        val id: Long,
        val label: String,
    )

    // Set while the native username action sheet is being built so the next
    // ItemOptions.show() can append "Copy ID" to it.
    private val pendingCopyId = ThreadLocal<CopyIdItem?>()

    fun install(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        installShowHook(xposed, classLoader)
        installOtherProfileRowClick(xposed, classLoader)

        // Prefer hooking the method that builds the native username sheet so
        // "Copy ID" is appended to the existing items. 13.x split this between
        // usernameRowClick (float, float, int, View) and editRow (int, View).
        var installed =
            runCatching {
                installUsernameRowClick(xposed, classLoader)
                true
            }.getOrElse {
                Log.w("Telegami", "ProfileDetails username row hook unavailable: ${it.message}")
                false
            }

        if (!installed) {
            installed =
                runCatching {
                    installEditRowClick(xposed, classLoader)
                    true
                }.getOrElse {
                    Log.w("Telegami", "ProfileDetails edit row hook unavailable: ${it.message}")
                    false
                }
        }

        if (!installed) {
            installRowHandler(xposed, classLoader)
        }
    }

    /**
     * Nekogram/Telegram 13.x builds the username popup inside
     * ProfileActivity.V3(float, float, int row, View). We let the native menu
     * keep its items (copy username, change username, ...) and only append
     * "Copy ID" before it is shown.
     */
    private fun installUsernameRowClick(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        val method = classLoader.findMethod(PROFILE_ACTIVITY_CN, "usernameRowClick", 4)
        xposed.hook(method).intercept { chain ->
            val args = chain.args.toMutableList()
            val rowIdx = args.getOrNull(2) as? Int
            val view = args.getOrNull(3) as? View
            try {
                if (rowIdx != null && view != null) {
                    val o = ProfileActivity(chain.thisObject!!)
                    if (rowIdx == o.usernameRow) {
                        copyIdFor(o)?.let { pendingCopyId.set(it) }
                    }
                }
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed before ${method.name}", throwable)
            }
            try {
                chain.proceed(args.toTypedArray())
            } finally {
                pendingCopyId.remove()
            }
        }
    }

    /**
     * Telegram 13.x builds the row popup in ProfileActivity.editRow(int row,
     * View). The username row is included there, so remember the owner id and
     * let ItemOptions.show() append "Copy ID".
     */
    private fun installEditRowClick(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        val method = classLoader.findMethod(PROFILE_ACTIVITY_CN, "editRow", 2)
        xposed.hook(method).intercept { chain ->
            val args = chain.args.toMutableList()
            val rowIdx = args.getOrNull(0) as? Int
            val view = args.getOrNull(1) as? View
            try {
                if (rowIdx != null && view != null) {
                    val o = ProfileActivity(chain.thisObject!!)
                    if (rowIdx == o.usernameRow) {
                        copyIdFor(o)?.let { pendingCopyId.set(it) }
                    }
                }
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed before ${method.name}", throwable)
            }
            try {
                chain.proceed(args.toTypedArray())
            } finally {
                pendingCopyId.remove()
            }
        }
    }

    private fun installShowHook(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        val method = classLoader.findMethod(ITEM_OPTIONS_CN, "show")
        xposed.hook(method).intercept { chain ->
            pendingCopyId.get()?.let { item ->
                pendingCopyId.remove()
                runCatching {
                    val msgCopyId = Telegami.getResource("msg_copy", "drawable") ?: 0
                    ItemOptions.from(chain.thisObject!!).add(
                        msgCopyId,
                        item.label,
                        Runnable {
                            AndroidUtilities.addToClipboard(item.id.toString())
                            val msg = i18n.get("CopiedToClipboardHint").replace("{item}", "ID")
                            Telegami.showToast(Toast.LENGTH_SHORT, msg)
                        },
                    )
                }.onFailure {
                    Log.e("Telegami", "Failed to append Copy ID to profile menu", it)
                }
            }
            chain.proceed(chain.args.toTypedArray())
        }
    }

    /**
     * The username row on other users'/chats' profiles is not clickable in
     * 13.x (only the QR icon is). After the profile data is bound, find the
     * username cell and give the whole row the same copy menu.
     */
    private fun installOtherProfileRowClick(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        val method = classLoader.findMethod(PROFILE_ACTIVITY_CN, "updateProfileData")
        xposed.hook(method).intercept { chain ->
            val result = chain.proceed(chain.args.toTypedArray())
            runCatching {
                val o = ProfileActivity(chain.thisObject!!)
                if (o.myProfile) return@runCatching
                val root = o.contentView as? View ?: return@runCatching
                root.post { attachUsernameRowListener(o, root) }
            }.onFailure {
                Log.e("Telegami", "Failed to attach username row listener", it)
            }
            result
        }
    }

    private fun attachUsernameRowListener(
        o: ProfileActivity,
        root: View,
    ) {
        val expected = resolveUsername(o) ?: return
        val row = findUsernameRow(root, expected) ?: return
        if (row.hasOnClickListeners()) return
        row.isClickable = true
        row.setOnClickListener { clicked ->
            if (findUsernameRow(clicked, expected) != null) {
                showFallbackCopyMenu(o, clicked)
            }
        }
    }

    private fun resolveUsername(o: ProfileActivity): String? {
        val msgCtrl = o.getMessagesController()
        val username =
            when {
                o.userId != 0L -> msgCtrl?.getUser(o.userId)?.username
                o.chatId != 0L -> msgCtrl?.getChat(o.chatId)?.username
                else -> null
            }
        return username?.takeIf { it.isNotEmpty() }?.let { "@$it" }
    }

    private fun findUsernameRow(
        view: View,
        expected: String,
    ): View? {
        if (view is TextView && view.text.toString() == expected) {
            return view.parent as? View
        }
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findUsernameRow(view.getChildAt(index), expected)?.let { return it }
            }
        }
        return null
    }

    /**
     * Fallback for older clients whose row handler is processOnClickOrPress.
     * Handles the known signatures across clients:
     *  - (Context, long, View, int, float, float)
     *  - (?, ?, int, View)
     *  - (int, View)
     */
    private fun installRowHandler(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        val method = classLoader.findMethod(PROFILE_ACTIVITY_CN, "processOnClickOrPress")
        xposed.hook(method).intercept { chain ->
            val args = chain.args.toMutableList()
            var handled = false
            try {
                val rowClick = extractRowClick(args)
                if (rowClick != null) {
                    val o = ProfileActivity(chain.thisObject!!)
                    if (rowClick.first == o.usernameRow) {
                        handled = showFallbackCopyMenu(o, rowClick.second)
                    }
                }
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed before ${method.name}", throwable)
            }
            if (!handled) {
                chain.proceed(args.toTypedArray())
            } else if (method.returnType == Void.TYPE) {
                null
            } else {
                false
            }
        }
    }

    private fun copyIdFor(o: ProfileActivity): CopyIdItem? {
        val userId = o.userId
        val chatId = o.chatId
        val msgCtrl = o.getMessagesController()
        return when {
            userId != 0L -> CopyIdItem(userId, i18n.get("ProfileCopyUserId"))
            chatId != 0L -> {
                if (msgCtrl?.getChat(chatId) == null) return null
                CopyIdItem(chatId, i18n.get("ProfileCopyChatId"))
            }
            else -> null
        }
    }

    private fun showFallbackCopyMenu(
        o: ProfileActivity,
        view: View,
    ): Boolean {
        val chatId = o.chatId
        val userId = o.userId
        val msgCtrl = o.getMessagesController()

        val (username, id, idLabel) =
            when {
                userId != 0L -> {
                    val user = msgCtrl?.getUser(userId) ?: return false
                    val username = user.username
                    if (username.isEmpty()) return false
                    Triple(username, userId, i18n.get("ProfileCopyUserId"))
                }

                chatId != 0L -> {
                    val chat = msgCtrl?.getChat(chatId) ?: return false
                    val username = chat.username
                    if (username.isEmpty()) return false
                    Triple(username, chatId, i18n.get("ProfileCopyChatId"))
                }

                else -> return false
            }

        val contentView = o.contentView as? ViewGroup ?: return false
        val resourcesProvider = o.resourcesProvider
        val msgCopyId = Telegami.getResource("msg_copy", "drawable") ?: 0

        val itemOptions = ItemOptions.makeOptions(contentView, resourcesProvider, view, false)
        itemOptions.setGravity(Gravity.LEFT)
        itemOptions
            .add(
                msgCopyId,
                i18n.get("ProfileCopyUsername"),
                Runnable {
                    AndroidUtilities.addToClipboard(username)
                    val msg = i18n.get("CopiedToClipboardHint").replace("{item}", "username")
                    Telegami.showToast(Toast.LENGTH_SHORT, msg)
                },
            ).add(
                msgCopyId,
                idLabel,
                Runnable {
                    AndroidUtilities.addToClipboard(id.toString())
                    val msg = i18n.get("CopiedToClipboardHint").replace("{item}", "ID")
                    Telegami.showToast(Toast.LENGTH_SHORT, msg)
                },
            )
        itemOptions.show()
        return true
    }

    private fun extractRowClick(args: List<Any?>): Pair<Int, View>? {
        if (args.size >= 6 && args[2] is View && args[3] is Int) {
            return (args[3] as Int) to (args[2] as View)
        }
        if (args.size >= 4 && args[2] is Int && args[3] is View) {
            return (args[2] as Int) to (args[3] as View)
        }
        if (args.size >= 2 && args[0] is Int && args[1] is View) {
            return (args[0] as Int) to (args[1] as View)
        }
        return null
    }
}
