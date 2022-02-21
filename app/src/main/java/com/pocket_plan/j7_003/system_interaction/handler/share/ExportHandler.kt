package com.pocket_plan.j7_003.system_interaction.handler.share

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.pocket_plan.j7_003.BuildConfig
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import java.io.File
import java.io.FileOutputStream
import org.threeten.bp.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * A simple class used to handle exporting of the apps save files.
 */
class ExportHandler(private val parentActivity: AppCompatActivity) {
    private lateinit var zipFile: File

    /**
     * Used to share logs if they exist on the device.
     */
    fun shareLog() {
        val file = File(parentActivity.filesDir, "Log.txt")

        if (!file.exists()) {
            return
        }

        val uri = FileProvider.getUriForFile(parentActivity,
            "${parentActivity.applicationContext.packageName}.provider", file)

        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "application/text"

        sharingIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)

        // actual start of sharing
        parentActivity.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    /**
     * Starts an activity to share a zip file, containing all save files.
     */
    fun shareAll() {
        backUpAsZip()   // creates and adds the backup file object to the StorageHandler

        // a uri to the backup file
        val uri = FileProvider.getUriForFile(parentActivity,
            "${BuildConfig.APPLICATION_ID}.provider", zipFile)

        // the intent used to share the zip archived backup
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "application/zip"

        sharingIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)

        // actual start of sharing
        parentActivity.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    /**
     * Starts an activity to share a json file, containing the requested modules save file.
     * @param id The storage id of the requested file.
     */
    fun shareById(id: StorageId) {
        // a uri to the backup file
        val uri = FileProvider.getUriForFile(parentActivity,
            "${parentActivity.applicationContext.packageName}.provider", StorageHandler.files[id]!!)

        // the intent used to share the zip archived backup
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "application/json"

        sharingIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)

        // actual start of sharing
        parentActivity.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    private fun backUpAsZip() {
        zipFile = File(parentActivity.filesDir.absolutePath,
            "pocket_plan_${BuildConfig.VERSION_NAME}_backup_${LocalDate.now()}.zip")
        val outputStream = FileOutputStream(zipFile)
        val zipStream = ZipOutputStream(outputStream)

        StorageHandler.files.forEach { (_, file) ->
            writeToZipFile(zipStream, file)
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