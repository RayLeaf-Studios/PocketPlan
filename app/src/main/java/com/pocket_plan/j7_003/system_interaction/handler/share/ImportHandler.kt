package com.pocket_plan.j7_003.system_interaction.handler.share

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayList
import com.pocket_plan.j7_003.data.notelist.NoteAdapter
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

    fun importFromJson(id: StorageId, inputStream: InputStream, file: File) {
        val outputStream = file.outputStream()

        val byteArray = ByteArray(1)
        var length = inputStream.read(byteArray)

        while (length != -1) {
            outputStream.write(byteArray)
            length = inputStream.read(byteArray)
        }

        outputStream.close()
        inputStream.close()

        val fileDir = "${parentActivity.filesDir}/"
        val oldFile = File("${fileDir}old_${id.s}")

        oldFile.writeText(StorageHandler.files[id]!!.readText())
        StorageHandler.files[id]!!.writeText(file.readText())

        if (!testFiles()) {
            StorageHandler.files[id]!!.writeText(oldFile.readText())
        }

        oldFile.delete()
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
        val newDir = File("${parentActivity.filesDir}/new/")
        val oldDir = File("${parentActivity.filesDir}/old/")
        var entryContent: String
        var cacheFile: File

        newDir.mkdir()
        oldDir.mkdir()

        File("${parentActivity.filesDir}/").listFiles()!!.forEach { oldFile ->
            if (oldFile.extension == "json") {
                File("${parentActivity.filesDir}/old/${oldFile.name}").writeText(oldFile.readText())
            }
        }

        StorageId.values().forEach {
            if (it.s != StorageId.ZIP.s) {
                cacheFile = File("${parentActivity.filesDir}/new/${it.s}")

                entryContent = zipFile.getInputStream(ZipEntry(it.s)).bufferedReader()
                    .use { reader -> reader.readText() }

                cacheFile.writeText(entryContent)
                newFiles[it] = cacheFile
            }
        }

        newFiles.forEach { (id, file) ->
            StorageHandler.files[id]?.writeText(file.readText())
        }

        if (!testFiles()) {
            File("${parentActivity.filesDir}/old/").listFiles()!!.forEach { currentFile ->
                File("${parentActivity.filesDir}/${currentFile.name}").writeText(currentFile.readText())
            }
        }

        newDir.deleteRecursively()
        oldDir.deleteRecursively()
    }

    internal fun browse(fileType: String, id: StorageId) {
        val chooseFileIntent = Intent(Intent.ACTION_GET_CONTENT)
        chooseFileIntent.type = "application/$fileType"
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
        parentActivity.startActivityForResult(Intent.createChooser(chooseFileIntent, "Choose file"), id.i)
    }

    private fun testFiles(): Boolean {
        return try {
            BirthdayList()
            NoteList()
            TodoList()
            SettingsManager.init()
            SleepReminder(parentActivity)
            ShoppingList()
            UserItemTemplateList()
            Toast.makeText(parentActivity, "Import successful!", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            // inform the user that the import didn't succeed
            Toast.makeText(parentActivity, "Import failed", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
