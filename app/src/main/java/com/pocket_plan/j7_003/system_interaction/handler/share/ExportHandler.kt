package com.pocket_plan.j7_003.system_interaction.handler.share

import android.content.Intent
import androidx.core.content.FileProvider
import com.pocket_plan.j7_003.BuildConfig
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ExportHandler {
    fun shareAll() {
        backUpAsZip()

        val uri = FileProvider.getUriForFile(
            MainActivity.act, "${BuildConfig.APPLICATION_ID}.provider",
            StorageHandler.files[StorageId.ZIP]!!)

        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "application/zip"

        sharingIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)

        MainActivity.act.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    fun shareById(id: StorageId) {
        val uri = FileProvider.getUriForFile(MainActivity.act,
            "${MainActivity.act.applicationContext.packageName}.provider", StorageHandler.files[id]!!)
        val sharingIntent = Intent(Intent.ACTION_SEND)

        sharingIntent.type = "application/json"
        sharingIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)

        MainActivity.act.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    fun shareShoppingList() {
        val uri = FileProvider.getUriForFile(MainActivity.act,
            "${MainActivity.act.applicationContext.packageName}.provider",
            StorageHandler.files[StorageId.SHOPPING]!!)
        val sharingIntent = Intent(Intent.ACTION_SEND)

        sharingIntent.type = "application/json"
        sharingIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)

        MainActivity.act.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    private fun backUpAsZip() {
        StorageHandler.createFile(StorageId.ZIP, StorageId.ZIP.s)
        val outputStream = FileOutputStream(StorageHandler.files[StorageId.ZIP])
        val zipStream = ZipOutputStream(outputStream)

        StorageHandler.files.forEach { (_, file) ->
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