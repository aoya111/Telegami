package com.aoya.telegami.hooks

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.FrameLayout
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
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object PreventSecretMediaDeletion {
    const val CHAT_ACTIVITY_CN = "org.telegram.ui.ChatActivity"
    const val SECRET_MEDIA_VIEWER_CN = "org.telegram.ui.SecretMediaViewer"
    const val EMPTY_MEDIA_LAMBDA_CN = "org.telegram.messenger.MessagesStorage\$emptyMediaLambda"

    private const val GALLERY_MENU_ID = 1000

    val galleryDrawable: Drawable? by lazy {
        Telegami.getDrawableResource("msg_gallery")
    }

    fun install(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        if (!Config.isFeatureEnabled("PreventSecretMediaDeletion")) return
        // 12.x deletes single-view media through ChatActivity.sendSecretMediaDelete.
        // 13.x no longer has that method, so the viewer onClose hook below is the
        // one that keeps the media alive there.
        runCatching {
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
        }.onFailure {
            Log.w("Telegami", "sendSecretMediaDelete hook unavailable: ${it.message}")
        }
        // The viewer deletes single-view/TTL media through its onClose callback.
        runCatching {
            val closePhoto = classLoader.findMethod(SECRET_MEDIA_VIEWER_CN, "closePhoto", 2)
            xposed.hook(closePhoto).intercept { chain ->
                try {
                    val viewer = chain.thisObject ?: return@intercept chain.proceed(chain.args.toTypedArray())
                    val fieldName = resolver.getField(SECRET_MEDIA_VIEWER_CN, "onClose")
                    viewer.javaClass.getDeclaredField(fieldName).apply { isAccessible = true }.set(viewer, null)
                } catch (throwable: Throwable) {
                    Log.e("Telegami", "Failed to clear secret-media onClose", throwable)
                }
                chain.proceed(chain.args.toTypedArray())
            }
        }.onFailure {
            Log.w("Telegami", "secret-media closePhoto hook unavailable: ${it.message}")
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
        } else if (Telegami.packageName == "tw.nekomimi.nekogram") {
            // 13.2.2 official removed MessagesStorage.emptyMessagesMedia:
            // media is no longer emptied separately from the message row, so
            // only the Nekogram inlined lambda needs the extra hook.
            installEmptyMediaLambdaHook(xposed, classLoader)
        }
        if (Telegami.packageName == "tw.nekomimi.nekogram") {
            // Nekogram 13.2.2 inlines openMedia into q.s0(ChatMessageCell, float, float).
            // The host class/method lives in nekogram.json as a virtual entry.
            val openMedia =
                classLoader.findMethod("org.telegram.ui.SecretMediaViewer\$openMediaInline", "openMedia", 3)
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
                    if (msgObj.isSecretMedia()) {
                        addDownloadItem(viewer, msgObj)
                        // Let the read receipt go out, but cancel the TTL-based
                        // deletion so the media stays after closing.
                        val owner = msgObj.messageOwner
                        val ttl = owner.ttl
                        if (ttl in 1 until Int.MAX_VALUE) {
                            owner.ttl = Int.MAX_VALUE
                        }
                    }
                } catch (throwable: Throwable) {
                    Log.e("Telegami", "Failed to add secret-media download item", throwable)
                }
                result
            }
        } else if (Telegami.packageName == "org.telegram.messenger") {
            // Telegram 13.x keeps openMedia on SecretMediaViewer. Telegram
            // 12.10.3 inlined it into ao.t0, so fall back to that.
            val openMedia =
                runCatching { classLoader.findMethod(SECRET_MEDIA_VIEWER_CN, "openMedia") }
                    .getOrElse { classLoader.findMethod("org.telegram.ui.ao", "t0") }
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
                    if (msgObj.isSecretMedia()) {
                        addDownloadItem(viewer, msgObj)
                        // 13.x schedules destruction when the viewer closes;
                        // MAX_VALUE makes that close path use the non-deleting
                        // branch, matching the Nekogram openMedia handling.
                        val owner = msgObj.messageOwner
                        val ttl = owner.ttl
                        if (ttl in 1 until Int.MAX_VALUE) {
                            owner.ttl = Int.MAX_VALUE
                        }
                    }
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

    /**
     * 13.2.2 inlines emptyMessagesMedia into the shared storage lambda
     * MessagesStorage$$ExternalSyntheticLambda30. Type 1 is the media-clear
     * body; skip it for messages that are being preserved.
     */
    private fun installEmptyMediaLambdaHook(
        xposed: XposedInterface,
        classLoader: ClassLoader,
    ) {
        runCatching {
            val lambdaClass = classLoader.loadClass(resolver.get(EMPTY_MEDIA_LAMBDA_CN))
            val run = lambdaClass.getDeclaredMethod("run").apply { isAccessible = true }
            xposed.hook(run).intercept { chain ->
                try {
                    val type = fieldValue(chain.thisObject, resolver.getField(EMPTY_MEDIA_LAMBDA_CN, "type")) as? Byte
                    if (type == 1.toByte()) {
                        val dialogId =
                            fieldValue(chain.thisObject, resolver.getField(EMPTY_MEDIA_LAMBDA_CN, "dialogId")) as? Long
                        val rawIds =
                            fieldValue(chain.thisObject, resolver.getField(EMPTY_MEDIA_LAMBDA_CN, "ids")) as? ArrayList<*>
                        val mIds = rawIds?.filterIsInstance<Int>() ?: emptyList()
                        if (dialogId != null && mIds.isNotEmpty()) {
                            if (!Globals.handleDeletedMessages(dialogId, mIds)) {
                                return@intercept null
                            }
                        }
                    }
                } catch (throwable: Throwable) {
                    Log.e("Telegami", "Hook callback failed before ${run.name}", throwable)
                }
                chain.proceed(chain.args.toTypedArray())
            }
        }.onFailure {
            Log.w("Telegami", "empty-media lambda hook unavailable: ${it.message}")
        }
    }

    private fun fieldValue(
        target: Any,
        name: String,
    ): Any? {
        var clazz: Class<*>? = target.javaClass
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name).apply { isAccessible = true }.get(target)
            } catch (_: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        return null
    }

    private fun addDownloadItem(
        viewer: SecretMediaViewer,
        msgObj: MessageObject,
    ) {
        val file = FileLoader.getInstance(viewer.currentAccount).getPathToMessage(msgObj.messageOwner)
        val menu = viewer.actionBar.menu ?: viewer.actionBar.createMenu()
        val gallery = galleryDrawable ?: return
        // 13.2.2 uses id 1 for the native edit action, so a dedicated id is
        // required to avoid hijacking an existing button.
        val downloadItem = (menu.getItem(GALLERY_MENU_ID) ?: menu.addItem(GALLERY_MENU_ID, gallery)) as FrameLayout
        downloadItem.setOnClickListener(
            View.OnClickListener {
                // Never let a mapping/host failure in this click handler take
                // the whole app down.
                runCatching {
                    val decryptedFile = SecretMedia.decrypt(file, viewer.isVideo)
                    if (decryptedFile == null) {
                        Log.w("Telegami", "Failed to decrypt secret media: $file")
                        return@OnClickListener
                    }
                    MediaController.saveFile(
                        decryptedFile.toString(),
                        viewer.parentActivity,
                        0,
                        null,
                        null,
                    ) {
                        decryptedFile.delete()
                        BulletinFactory
                            .createSaveToGalleryBulletin(viewer.containerView, viewer.isVideo, null)
                            .show()
                    }
                }.onFailure {
                    Log.e("Telegami", "Failed to save secret media", it)
                }
            },
        )
        (viewer.secretDeleteTimer as? View)?.visibility = View.GONE
    }
}
