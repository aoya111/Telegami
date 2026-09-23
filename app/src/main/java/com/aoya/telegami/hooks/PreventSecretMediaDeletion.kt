package com.aoya.telegami.hooks

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.aoya.telegami.Telegami
import com.aoya.telegami.service.Config
import com.aoya.telegami.util.SecretMedia
import com.aoya.telegami.virt.messenger.FileLoader
import com.aoya.telegami.virt.messenger.MediaController
import com.aoya.telegami.virt.messenger.MessageObject
import com.aoya.telegami.virt.ui.SecretMediaViewer
import com.aoya.telegami.virt.ui.components.BulletinFactory
import com.aoya.telegami.util.findMethod
import java.io.File
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver
import io.github.libxposed.api.XposedInterface
import android.util.Log

object PreventSecretMediaDeletion {
    const val CHAT_ACTIVITY_CN = "org.telegram.ui.ChatActivity"
    const val MESSAGES_STORAGE_CN = "org.telegram.messenger.MessagesStorage"
    const val SECRET_MEDIA_VIEWER_CN = "org.telegram.ui.SecretMediaViewer"

    val galleryDrawable: Drawable? by lazy {
        Telegami.getDrawableResource("msg_gallery")
    }

    fun install(xposed: XposedInterface, classLoader: ClassLoader) {
        if (!Config.isFeatureEnabled("PreventSecretMediaDeletion")) return
        val sendDelete = classLoader.findMethod(CHAT_ACTIVITY_CN, "sendSecretMediaDelete")
        xposed.hook(sendDelete).intercept { chain ->
            val args = chain.args.toMutableList()
            var result: Any? = null
            var hasResult = false
            try { hasResult = true } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed before ${sendDelete.name}", throwable)
            }
            if (!hasResult) result = chain.proceed(args.toTypedArray())
            result
        }
        if (Telegami.packageName == "xyz.nextalone.nagram") {
            val cName1 = "org.telegram.ui.Stories.StoriesStorage\$\$ExternalSyntheticLambda5"

            val lambdaClass = classLoader.loadClass(cName1)
            val run = lambdaClass.declaredMethods.first { it.name == "run" }.apply { isAccessible = true }
            xposed.hook(run).intercept { chain ->
                val args = chain.args.toMutableList()
                var result: Any? = null
                var hasResult = false
                try {
                        val dialogId = lambdaClass.getDeclaredField("f\$1").apply { isAccessible = true }.get(chain.thisObject) as? Long ?: return@intercept chain.proceed(args.toTypedArray())
                        val mIds = lambdaClass.getDeclaredField("f\$2").apply { isAccessible = true }.get(chain.thisObject) as? ArrayList<Int> ?: return@intercept chain.proceed(args.toTypedArray())
                        if (Globals.handleDeletedMessages(dialogId, mIds)) return@intercept chain.proceed(args.toTypedArray())
                        hasResult = true
                } catch (throwable: Throwable) {
                    Log.e("Telegami", "Hook callback failed before ${run.name}", throwable)
                }
                if (!hasResult) result = chain.proceed(args.toTypedArray())
                result
            }
        } else {
            val emptyMedia = classLoader.findMethod(MESSAGES_STORAGE_CN, "emptyMessagesMedia")
            xposed.hook(emptyMedia).intercept { chain ->
                val args = chain.args.toMutableList()
                var result: Any? = null
                var hasResult = false
                try {
                        val dialogId = args[0] as Long
                        val mIds = args[1] as ArrayList<Int>
                        if (Globals.handleDeletedMessages(dialogId, mIds)) return@intercept chain.proceed(args.toTypedArray())
                        hasResult = true
                } catch (throwable: Throwable) {
                    Log.e("Telegami", "Hook callback failed before ${emptyMedia.name}", throwable)
                }
                if (!hasResult) result = chain.proceed(args.toTypedArray())
                result
            }
        }
        val openMedia = classLoader.findMethod(SECRET_MEDIA_VIEWER_CN, "openMedia")
        xposed.hook(openMedia).intercept { chain ->
            val args = chain.args.toMutableList()
            val result = chain.proceed(args.toTypedArray())
            try {
                    val o = chain.thisObject?.let { SecretMediaViewer(it) } ?: return@intercept result
                    val msgObj = args[0]?.let { MessageObject(it) } ?: return@intercept result
                    val file =
                        msgObj.messageOwner?.let { FileLoader.getInstance(o.currentAccount).getPathToMessage(it) } ?: return@intercept result
                    var menu = o.actionBar.menu
                    var downloadItem: FrameLayout? = null
                    if (menu == null) {
                        menu = o.actionBar.createMenu()
                        val resDownload = galleryDrawable ?: return@intercept result
                        downloadItem = menu.addItem(1, resDownload) as FrameLayout
                    } else {
                        downloadItem = menu.getItem(1) as FrameLayout
                    }
                    downloadItem
                        .setOnClickListener(
                            View.OnClickListener { view ->
                                val f = SecretMedia.decrypt(file, o.isVideo)
                                MediaController.saveFile(
                                    f.toString(),
                                    o.parentActivity,
                                    0,
                                    null,
                                    null,
                                ) { uri ->
                                    f?.delete()
                                    BulletinFactory.createSaveToGalleryBulletin(o.containerView, o.isVideo, null).show()
                                }
                            },
                        )

                    val secretDeleteTimer = o.secretDeleteTimer as FrameLayout
                    secretDeleteTimer.visibility = View.GONE
            } catch (throwable: Throwable) {
                Log.e("Telegami", "Hook callback failed after ${openMedia.name}", throwable)
            }
            result
        }
    }
}
