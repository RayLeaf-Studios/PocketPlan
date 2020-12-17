package com.pocket_plan.j7_003.system_interaction.handler.share

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.pocket_plan.j7_003.BuildConfig
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ExportHandler(val parentActivity: AppCompatActivity) {
    fun shareAll() {
        backUpAsZip()

        val uri = FileProvider.getUriForFile(
            parentActivity, "${BuildConfig.APPLICATION_ID}.provider",
            MainActivity.storageHandler.files[StorageId.ZIP]!!)

        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "application/zip"

        sharingIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)

        parentActivity.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    fun shareById(id: StorageId) {
        val uri = FileProvider.getUriForFile(parentActivity,
            "${parentActivity.applicationContext.packageName}.provider", MainActivity.storageHandler.files[id]!!)
        val sharingIntent = Intent(Intent.ACTION_SEND)

        sharingIntent.type = "application/json"
        sharingIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)

        parentActivity.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    private fun backUpAsZip() {
        MainActivity.storageHandler.createFile(StorageId.ZIP, StorageId.ZIP.s)
        val outputStream = FileOutputStream(MainActivity.storageHandler.files[StorageId.ZIP])
        val zipStream = ZipOutputStream(outputStream)

        MainActivity.storageHandler.files.forEach { (_, file) ->
            if (file.name != StorageId.ZIP.s) {
                writeToZipFile(zipStream, file)
            }
        }

        zipStream.close()
        outputStream.close()
    }

    private fun writeToZipFile(zipStream: ZipOutputStream, file: File) {
        val zipEntry = ZipEntry(file.name)
        val fis = file.inputStream()
        zipStream.putNextEntry(zipEntry)

        val bytes = ByteArray(1)
        var length: Int

        length = fis.read(bytes)
        while (length >= 0) {
            zipStream.write(bytes, 0, length)
            length = fis.read(bytes)
        }

        zipStream.closeEntry()
        fis.close()
    }
}