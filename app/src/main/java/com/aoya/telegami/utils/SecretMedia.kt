package com.aoya.telegami.utils

import com.aoya.telegami.Telegami
import com.aoya.telegami.virt.messenger.FileLoader
import com.aoya.telegami.virt.messenger.secretmedia.EncryptedFileInputStream
import java.io.File
import java.io.FileOutputStream

object SecretMedia {
    private const val VIDEO_CHUNK_SIZE = 1024 * 1024 // 1MB
    private const val IMAGE_CHUNK_SIZE = 512 * 1024 // 512KB

    /**
     * Decrypts a secret media file to a temporary file
     * @return Temporary decrypted file, or null if encrypted file or key doesn't exist
     */
    fun decrypt(
        encFile: File,
        isVideo: Boolean,
    ): File? {
        val encryptedFile = File(encFile.absolutePath + ".enc")
        if (!encryptedFile.exists()) return null

        val keyFile = File(FileLoader.getInternalCacheDir(), "${encryptedFile.name}.key")
        if (!keyFile.exists()) return null

        val extension = if (isVideo) "mp4" else "jpg"
        val chunkSize = if (isVideo) VIDEO_CHUNK_SIZE else IMAGE_CHUNK_SIZE

        val tempDir = FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE)
        val outputFile = File(tempDir, "temp_decrypt_${System.currentTimeMillis()}.$extension")

        return if (decryptToFile(encryptedFile, keyFile, outputFile, chunkSize)) {
            outputFile
        } else {
            null
        }
    }

    /**
     * Decrypts an encrypted file to a specific output file
     * @return true if successful, false otherwise
     */
    fun decryptToFile(
        encryptedFile: File,
        keyFile: File,
        outputFile: File,
        chunkSize: Int = VIDEO_CHUNK_SIZE,
    ): Boolean =
        try {
            val buffer = ByteArray(chunkSize)

            EncryptedFileInputStream.create(encryptedFile, keyFile).use { input ->
                FileOutputStream(outputFile).use { output ->
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Telegami.logw("Failed to decrypt to file: ${e.message}")
            false
        }
}
