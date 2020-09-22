package com.pocket_plan.j7_003.system_interaction.handler.share

import android.content.Intent
import androidx.core.content.FileProvider
import com.pocket_plan.j7_003.BuildConfig
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ShareHandler() {
    fun shareAll() {
        StorageHandler.createFile(StorageId.ZIP, "bundle.zip")
        val zipStream = ZipOutputStream(StorageHandler.files[StorageId.ZIP]!!.outputStream())

        StorageHandler.files.forEach { (_, file) ->
            if (file.name != "bundle.zip") {
                writeToZipFile(zipStream, file)
            }
        }

        StorageHandler.files[StorageId.ZIP]!!.outputStream().close()
        zipStream.close()

        val uri = FileProvider.getUriForFile(
            MainActivity.act, "${BuildConfig.APPLICATION_ID}.provider",
            StorageHandler.files[StorageId.ZIP]!!)

        val sharingIntent = Intent(Intent.ACTION_SEND)

        sharingIntent.data = uri
        sharingIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)

        MainActivity.act.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    fun shareById(id: StorageId) {
        val uri = FileProvider.getUriForFile(MainActivity.act,
            "${MainActivity.act.applicationContext.packageName}.provider", StorageHandler.files[id]!!)
        val sharingIntent = Intent(Intent.ACTION_SEND)

        sharingIntent.data = uri
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)

        MainActivity.act.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    private fun writeToZipFile(zipStream: ZipOutputStream, file: File) {
        val path = file.absolutePath

        val zipEntry = ZipEntry(path)
        val fis = file.inputStream()
        zipStream.putNextEntry(zipEntry)

        val bytes = ByteArray(8)
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