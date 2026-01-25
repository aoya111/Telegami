package com.aoya.telegami.hooks

import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.aoya.telegami.Telegami
import com.aoya.telegami.data.DeletedMessage
import com.aoya.telegami.utils.Hook
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.virt.messenger.FileLoader
import com.aoya.telegami.virt.messenger.MediaController
import com.aoya.telegami.virt.messenger.MessageObject
import com.aoya.telegami.virt.messenger.secretmedia.EncryptedFileInputStream
import com.aoya.telegami.virt.ui.SecretMediaViewer
import com.aoya.telegami.virt.ui.components.BulletinFactory
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class PreventSecretMediaDeletion :
    Hook(
        "prevent_secret_media_deletion",
        "Prevent Deletion of Secret Media.",
    ) {
    override fun init() {
        findAndHook("org.telegram.ui.ChatActivity", "sendSecretMediaDelete", HookStage.BEFORE) { param ->
            param.setResult(null)
        }

        findAndHook(
            "org.telegram.messenger.MessagesStorage",
            "emptyMessagesMedia",
            HookStage.BEFORE,
        ) { param ->
            val dialogId = param.arg<Long>(0)
            val mIds = param.arg<ArrayList<Int>>(1)
            if (mIds.isEmpty()) return@findAndHook

            if (Globals.allowMsgDelete.compareAndSet(true, false)) {
                Globals.coroutineScope.launch {
                    db.deletedMessageDao().deleteAllByIds(mIds, dialogId)
                }
                return@findAndHook
            }

            Globals.coroutineScope.launch {
                db.deletedMessageDao().insertAll(
                    mIds.map { mid ->
                        DeletedMessage(id = mid, dialogId = dialogId)
                    },
                )
            }

            param.setResult(null)
        }

        fun decryptSecretMediaStreaming(
            encFile: File,
            isVideo: Boolean,
        ): File? {
            val file = File(encFile.getAbsolutePath() + ".enc")
            if (!file.exists()) return null
            val keyFile = File(FileLoader.getInternalCacheDir(), file.getName() + ".key")
            if (!keyFile.exists()) return null

            val extension = if (isVideo) "mp4" else "jpg"
            val chunkSize = if (isVideo) 1024 * 1024 else 512 * 1024

            val tempDir = FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE)
            val tempFile = File(tempDir, "temp_decrypt_${System.currentTimeMillis()}.$extension")

            val buffer = ByteArray(chunkSize)

            EncryptedFileInputStream.create(file, keyFile).use { input ->
                FileOutputStream(tempFile).use { output ->
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            return tempFile
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
                        val f = decryptSecretMediaStreaming(file, o.isVideo)
                        MediaController.saveFile(
                            f.toString(),
                            o.parentActivity,
                            0,
                            null,
                            null,
                        ) { uri ->
                            f?.delete()
                            if (Telegami.packageName == "tw.nekomimi.nekogram") {
                                Telegami.showToast(Toast.LENGTH_SHORT, getStringResource("PhotoSavedHint"))
                            } else {
                                BulletinFactory.createSaveToGalleryBulletin(o.containerView, o.isVideo, null).show()
                            }
                        }
                    },
                )

            val secretDeleteTimer = o.secretDeleteTimer as FrameLayout
            secretDeleteTimer.visibility = View.GONE
        }
    }
}
