package com.aoya.telegami.hooks

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.aoya.telegami.Telegami
import com.aoya.telegami.service.Config
import com.aoya.telegami.util.SecretMedia
import com.aoya.telegami.util.findMethod
import com.aoya.telegami.virt.messenger.FileLoader
import com.aoya.telegami.virt.messenger.MediaController
import com.aoya.telegami.virt.messenger.MessageObject
import com.aoya.telegami.virt.ui.SecretMediaViewer
import com.aoya.telegami.virt.ui.components.BulletinFactory
import io.github.libxposed.api.XposedInterface
import java.io.File
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object PreventSecretMediaDeletion {
    const val CHAT_ACTIVITY_CN = "org.telegram.ui.ChatActivity"
    const val MESSAGES_STORAGE_CN = "org.telegram.messenger.MessagesStorage"
    const val SECRET_MEDIA_VIEWER_CN = "org.telegram.ui.SecretMediaViewer"

    val galleryDrawable: Drawable? by lazy {
        Telegami.getDrawableResource("msg_gallery")
    }

    fun install(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        if (!Config.isFeatureEnabled("PreventSecretMediaDeletion")) return
        val sendDelete = classLoader.findMethod(CHAT_ACTIVITY_CN, "sendSecretMediaDelete")
        xposed.hook(sendDelete).intercept { chain ->
            val args = chain.args.toMutableList()
            var result: Any? = null
            var hasResult = false
            try {
                hasResult = true
            } catch (throwable: Throwable) {
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
                    val dialogId =
                        lambdaClass.getDeclaredField("f\$1").apply { isAccessible = true }.get(chain.thisObject) as? Long
                            ?: return@intercept chain.proceed(args.toTypedArray())
                    val mIds =
                        lambdaClass.getDeclaredField("f\$2").apply { isAccessible = true }.get(chain.thisObject) as? ArrayList<Int>
                            ?: return@intercept chain.proceed(args.toTypedArray())
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
        if (Telegami.packageName == "tw.nekomimi.nekogram") {
            // Nekogram 12.10.3 inlines openMedia into u.R(w32, float, float).
            val openMedia = classLoader.findMethod("org.telegram.ui.u", "R", 3)
            xposed.hook(openMedia).intercept { chain ->
                val result = chain.proceed(chain.args.toTypedArray())
                try {
                    val viewerClass = classLoader.loadClass(resolver.get(SECRET_MEDIA_VIEWER_CN))
                    val field =
                        viewerClass
                            .getDeclaredField(resolver.getField(SECRET_MEDIA_VIEWER_CN, "activeViewer"))
                            .apply { isAccessible = true }
                    val viewer = field.get(null)?.let(::SecretMediaViewer) ?: return@intercept result
                    val msgObj = viewer.currentMessageObject ?: return@intercept result
                    if (msgObj.isSecretMedia()) addDownloadItem(viewer, msgObj)
                } catch (throwable: Throwable) {
                    Log.e("Telegami", "Failed to add secret-media download item", throwable)
                }
                result
            }
        } else if (Telegami.packageName == "org.telegram.messenger") {
            // Telegram 12.10.3 inlines SecretMediaViewer.openMedia into ao.t0.
            val openMedia = classLoader.findMethod("org.telegram.ui.ao", "t0")
            xposed.hook(openMedia).intercept { chain ->
                val result = chain.proceed(chain.args.toTypedArray())
                try {
                    val viewerClass = classLoader.loadClass(resolver.get(SECRET_MEDIA_VIEWER_CN))
                    val field =
                        viewerClass
                            .getDeclaredField(resolver.getField(SECRET_MEDIA_VIEWER_CN, "activeViewer"))
                            .apply { isAccessible = true }
                    val viewer = field.get(null)?.let(::SecretMediaViewer) ?: return@intercept result
                    val msgObj = viewer.currentMessageObject ?: return@intercept result
                    if (msgObj.isSecretMedia()) addDownloadItem(viewer, msgObj)
                } catch (throwable: Throwable) {
                    Log.e("Telegami", "Failed to add secret-media download item", throwable)
                }
                result
            }
        } else {
            val openMedia = classLoader.findMethod(SECRET_MEDIA_VIEWER_CN, "openMedia")
            xposed.hook(openMedia).intercept { chain ->
                val result = chain.proceed(chain.args.toTypedArray())
                try {
                    val viewer = chain.thisObject?.let(::SecretMediaViewer) ?: return@intercept result
                    val msgObj = chain.args.firstOrNull()?.let(::MessageObject) ?: return@intercept result
                    if (msgObj.isSecretMedia()) addDownloadItem(viewer, msgObj)
                } catch (throwable: Throwable) {
                    Log.e("Telegami", "Failed to add secret-media download item", throwable)
                }
                result
            }
        }
    }

    private fun addDownloadItem(
        viewer: SecretMediaViewer,
        msgObj: MessageObject,
    ) {
        val file = msgObj.messageOwner?.let { FileLoader.getInstance(viewer.currentAccount).getPathToMessage(it) } ?: return
        val menu = viewer.actionBar.menu ?: viewer.actionBar.createMenu()
        val downloadItem = (menu.getItem(1) ?: menu.addItem(1, galleryDrawable ?: return)) as FrameLayout
        downloadItem.setOnClickListener(
            View.OnClickListener {
                val decryptedFile = SecretMedia.decrypt(file, viewer.isVideo)
                MediaController.saveFile(
                    decryptedFile.toString(),
                    viewer.parentActivity,
                    0,
                    null,
                    null,
                ) {
                    decryptedFile?.delete()
                    BulletinFactory.createSaveToGalleryBulletin(viewer.containerView, viewer.isVideo, null).show()
                }
            },
        )
        (viewer.secretDeleteTimer as? View)?.visibility = View.GONE
    }
}
