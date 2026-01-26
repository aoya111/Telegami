package com.aoya.telegami.hooks

import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.aoya.telegami.Telegami
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.SecretMedia
import com.aoya.telegami.virt.messenger.FileLoader
import com.aoya.telegami.virt.messenger.MediaController
import com.aoya.telegami.virt.messenger.MessageObject
import com.aoya.telegami.virt.ui.SecretMediaViewer
import com.aoya.telegami.virt.ui.components.BulletinFactory
import de.robv.android.xposed.XposedHelpers.getLongField
import de.robv.android.xposed.XposedHelpers.getObjectField
import java.io.File

class PreventSecretMediaDeletion :
    Hook(
        "prevent_secret_media_deletion",
        "Prevent Deletion of Secret Media.",
    ) {
    override fun init() {
        findAndHook("org.telegram.ui.ChatActivity", "sendSecretMediaDelete", HookStage.BEFORE) { param ->
            param.setResult(null)
        }

        if (Telegami.packageName == "xyz.nextalone.nagram") {
            findAndHook(
                "org.telegram.messenger.MessagesStorage\$\$ExternalSyntheticLambda13",
                "run",
                HookStage.BEFORE,
            ) { param ->
                val dialogId = getLongField(param.thisObject(), "f\$2")
                val mIds = getObjectField(param.thisObject(), "f\$1") as ArrayList<Int>
                Globals.storeDeletedMessages(dialogId, mIds)
                param.setResult(null)
            }
        } else {
            findAndHook(
                "org.telegram.messenger.MessagesStorage",
                "emptyMessagesMedia",
                HookStage.BEFORE,
            ) { param ->
                val dialogId = param.arg<Long>(0)
                val mIds = param.arg<ArrayList<Int>>(1)
                Globals.storeDeletedMessages(dialogId, mIds)
                param.setResult(null)
            }
        }

        findAndHook("org.telegram.ui.SecretMediaViewer", "openMedia", HookStage.AFTER) { param ->
            val o = SecretMediaViewer(param.thisObject())
            val msgObj = MessageObject(param.arg<Any>(0))
            val file = FileLoader.getInstance(o.currentAccount).getPathToMessage(msgObj.messageOwner)
            var menu = o.actionBar.menu
            var downloadItem: FrameLayout? = null
            if (menu == null) {
                menu = o.actionBar.createMenu()
                val resDownload = getDrawableResource("msg_gallery") ?: return@findAndHook
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
