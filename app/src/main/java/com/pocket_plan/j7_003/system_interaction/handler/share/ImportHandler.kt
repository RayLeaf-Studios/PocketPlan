package com.pocket_plan.j7_003.system_interaction.handler.share

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayList
import com.pocket_plan.j7_003.data.notelist.NoteList
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.data.shoppinglist.ShoppingList
import com.pocket_plan.j7_003.data.shoppinglist.UserItemTemplateList
import com.pocket_plan.j7_003.data.sleepreminder.SleepReminder
import com.pocket_plan.j7_003.data.todolist.TodoList
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ImportHandler(private val parentActivity: Activity) {
    private val newFiles: EnumMap<StorageId, File> = EnumMap(StorageId::class.java)
    private var oldFiles: EnumMap<StorageId, File> = EnumMap(StorageId::class.java)

    fun importFromJson() {
        TODO()
    }

    fun importFromZip(zipInputStream: InputStream, file: File) {
        val outputStream = file.outputStream()

        val byteArray = ByteArray(8)
        var length = zipInputStream.read(byteArray)

        while (length != -1) {
            outputStream.write(byteArray)
            length = zipInputStream.read(byteArray)
        }

        outputStream.close()
        zipInputStream.close()

        val zipFile = ZipFile(file)
        val dir = File("${parentActivity.filesDir}/new/")
        var entryContent: String
        var currentFile: File

        dir.mkdir()
        StorageId.values().forEach {
            if (it.s != StorageId.ZIP.s) {
                currentFile = File("${parentActivity.filesDir}/new/${it.s}")

                entryContent = zipFile.getInputStream(ZipEntry(it.s)).bufferedReader()
                    .use { reader -> reader.readText() }

                currentFile.writeText(entryContent)
                newFiles[it] = currentFile
            }
        }

        testFiles()
        dir.deleteRecursively()
    }

    internal fun browse(fileType: String, requestCode: Int) {
        val permission = ActivityCompat.checkSelfPermission(parentActivity,
            Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            parentActivity.requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
        }

        val chooseFileIntent = Intent(Intent.ACTION_GET_CONTENT)
        chooseFileIntent.type = "application/$fileType"
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
        parentActivity.startActivityForResult(Intent.createChooser(chooseFileIntent, "Choose file"), requestCode)
    }

    private fun testFiles() {
        saveCurrentStorage()
        overrideStorageReferences()

        try {
            BirthdayList()
            NoteList()
            TodoList()
            SettingsManager.init()
            SleepReminder()
            ShoppingList()
            UserItemTemplateList()

            resetStorageReferences()

            newFiles.forEach { (id, file) ->
                StorageHandler.files[id]?.writeText(file.readText())
            }
        } catch (e: Exception) {
            // inform the user that the import didn't succeed
            Toast.makeText(parentActivity, "Couldn't import!", Toast.LENGTH_LONG).show()
            resetStorageReferences()
        }
    }

    private fun saveCurrentStorage() {
        StorageHandler.files.forEach { (id, file) ->
            oldFiles[id] = file
        }
    }

    private fun overrideStorageReferences() {
        newFiles.forEach { (id, file) ->
            StorageHandler.files[id] = file
        }
    }

    private fun resetStorageReferences() {
        oldFiles.forEach { (id, file) ->
            StorageHandler.files[id] = file
        }
    }
}
