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
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import java.io.File
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

object PreventSecretMediaDeletion : YukiBaseHooker() {
    const val CHAT_ACTIVITY_CN = "org.telegram.ui.ChatActivity"
    const val MESSAGES_STORAGE_CN = "org.telegram.messenger.MessagesStorage"
    const val SECRET_MEDIA_VIEWER_CN = "org.telegram.ui.SecretMediaViewer"
    val chatActivityClass by lazyClass(resolver.get(CHAT_ACTIVITY_CN))
    val messagesStorageClass by lazyClass(resolver.get(MESSAGES_STORAGE_CN))
    val secretMediaViewerClass by lazyClass(resolver.get(SECRET_MEDIA_VIEWER_CN))

    val galleryDrawable: Drawable? by lazy {
        Telegami.getDrawableResource("msg_gallery")
    }

    override fun onHook() {
        if (!Config.isFeatureEnabled("PreventSecretMediaDeletion")) return
        chatActivityClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(CHAT_ACTIVITY_CN, "sendSecretMediaDelete")
            }.hook {
                before {
                    resultNull()
                }
            }
        if (Telegami.packageName == "xyz.nextalone.nagram") {
            val cName1 = "org.telegram.ui.Stories.StoriesStorage\$\$ExternalSyntheticLambda5"

            cName1
                .toClass()
                .resolve()
                .firstMethod {
                    name = "run"
                }.hook {
                    before {
                        val dialogId =
                            instance
                                .asResolver()
                                .firstField {
                                    name = "f\$1"
                                }.get<Long>() ?: return@before
                        val mIds =
                            instance
                                .asResolver()
                                .firstField {
                                    name = "f\$2"
                                }.get() as ArrayList<Int> ?: return@before
                        if (Globals.handleDeletedMessages(dialogId, mIds)) return@before
                        resultNull()
                    }
                }
        } else {
            messagesStorageClass
                .resolve()
                .firstMethod {
                    name = resolver.getMethod(MESSAGES_STORAGE_CN, "emptyMessagesMedia")
                }.hook {
                    before {
                        val dialogId = args[0] as Long
                        val mIds = args[1] as ArrayList<Int>
                        if (Globals.handleDeletedMessages(dialogId, mIds)) return@before
                        resultNull()
                    }
                }
        }
        secretMediaViewerClass
            .resolve()
            .firstMethod {
                name = resolver.getMethod(SECRET_MEDIA_VIEWER_CN, "openMedia")
            }.hook {
                after {
                    val o = instance?.let { SecretMediaViewer(it) } ?: return@after
                    val msgObj = args[0]?.let { MessageObject(it) } ?: return@after
                    val file =
                        msgObj.messageOwner?.let { FileLoader.getInstance(o.currentAccount).getPathToMessage(it) } ?: return@after
                    var menu = o.actionBar.menu
                    var downloadItem: FrameLayout? = null
                    if (menu == null) {
                        menu = o.actionBar.createMenu()
                        val resDownload = galleryDrawable ?: return@after
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
                }
            }
    }
}
