package com.pocket_plan.j7_003.system_interaction.handler.share

import SleepReminder
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayList
import com.pocket_plan.j7_003.data.notelist.NoteDirList
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.data.shoppinglist.ShoppingListWrapper
import com.pocket_plan.j7_003.data.shoppinglist.UserItemTemplateList
import com.pocket_plan.j7_003.data.todolist.TodoList
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.zip.ZipFile

/**
 * A simple class to handle the process of importing save files.
 * Needs an activity to be initialized, so tests and directory paths
 * can be done/set correctly.
 */
class ImportHandler(private val parentActivity: Activity) {
    private val newFiles: EnumMap<StorageId, File> = EnumMap(StorageId::class.java)

    /**
     * Handles the import for a specific module from json files.
     * The process goes as follows:
     *      1. A copy of the selected file is made,
     *      2. A rollback file of the current module file content is made,
     *      3. The module file is overwritten with the new files content,
     *      4. All modules are tested
     *          - if test fails changes are rolled back
     *      5. Temporary files are deleted.
     * @param id The storage id of the module to be imported.
     * @param inputStream An input stream to the file picked by the user.
     * @param file A file to write the picked files content to.
     */
    fun importFromJson(id: StorageId, inputStream: InputStream, file: File) {
        // used to copy the content of the picked file
        val outputStream = file.outputStream()

        // byte array used to read blockwise from the selected file
        val byteArray = ByteArray(8192)
        var length = inputStream.read(byteArray)

        // copies the content of the input stream to the given file
        while (length != -1) {
            outputStream.write(byteArray, 0, length)
            length = inputStream.read(byteArray)
        }

        outputStream.close()
        inputStream.close()

        // creates the file object for the rollback file
        val fileDir = "${parentActivity.filesDir}/"
        val oldFile = File("${fileDir}old_${id.s}")

        // copies the current modules file content to the rollback file
        oldFile.writeText(
            StorageHandler.readJsonFromFile(
                StorageHandler.files[id],
                fallbackText = fallbackJson(id)
            ) ?: fallbackJson(id)
        )
        // overwrites the content of the modules file with the selected files content
        StorageHandler.writeTextToFile(StorageHandler.files[id], file.readText())

        if (!testFiles()) { // rollbacks the file if the file couldn't be read correctly
            StorageHandler.writeTextToFile(StorageHandler.files[id], oldFile.readText())
        }

        // deletes the rollback file
        oldFile.delete()
    }

    /**
     * Handles the import for all modules from zip files. The process goes as follows:
     *      1. A copy of the user selected file is made,
     *      2. A rollback file of all module files content is made,
     *      3. The module files are overwritten with the new file contents,
     *      4. All modules are tested
     *          - if test fails changes are rolled back
     *      5. Temporary files are deleted.
     * @param zipInputStream An input stream to the zip file picked by the user.
     * @param file A file to write the picked files content to.
     */
    fun importFromZip(zipInputStream: InputStream, file: File) {
        // used to copy the content of the picked file
        val outputStream = file.outputStream()

        // byte array used to read blockwise from the selected file
        val byteArray = ByteArray(8192)
        var length = zipInputStream.read(byteArray)

        // copies the content of the input stream to the given file,
        // only writing as many bytes as were actually read
        while (length != -1) {
            outputStream.write(byteArray, 0, length)
            length = zipInputStream.read(byteArray)
        }

        outputStream.close()
        zipInputStream.close()

        // zip file object for the rollback file
        val zipFile = ZipFile(file)

        // paths to store the newly read and the rollback files
        val newDir = File("${parentActivity.filesDir}/new/")
        val oldDir = File("${parentActivity.filesDir}/old/")

        var entryContent: String    // content of a single entry in the zip file
        var cacheFile: File // a cache file used to unpack the zip file

        // creating the aforementioned directories
        newDir.mkdir()
        oldDir.mkdir()
        newFiles.clear()

        // copy content of each module file to a rollback file in the /old/ directory
        File("${parentActivity.filesDir}/").listFiles()!!.forEach { oldFile ->
            if (oldFile.extension == "json") {
                File("${parentActivity.filesDir}/old/${oldFile.name}")
                    .writeText(oldFile.readText())
            }
        }

        // unzip all all entries from selected file into the /new/ directory
        StorageId.values().forEach { id ->
            val entry = zipFile.getEntry(id.s)
            if (entry == null && id == StorageId.SHOPPING) {
                return@forEach
            }

            // the cache file is created with the corresponding name of the modules file name
            cacheFile = File("${parentActivity.filesDir}/new/${id.s}")

            // getting the content from the requested zip entry
            // (only file names from storage ids are valid)
            entryContent = if (entry != null) {
                zipFile.getInputStream(entry).bufferedReader()
                    .use { reader -> reader.readText() }
            } else {
                fallbackJson(id)
            }

            // the read in content is stored in the new file
            cacheFile.writeText(entryContent)
            newFiles[id] = cacheFile    // the file is added to a map to be easier managed
        }

        // the new files are used to overwrite their corresponding module files
        newFiles.forEach { (id, file) ->
            StorageHandler.createJsonFile(id, fallbackJson(id))
            StorageHandler.writeTextToFile(StorageHandler.files[id], file.readText())
        }

        // test of all file, if it fails all files are rolled back
        if (!testFiles()) {
            File("${parentActivity.filesDir}/old/").listFiles()!!.forEach { currentFile ->
                StorageHandler.writeTextToFile(
                    File("${parentActivity.filesDir}/${currentFile.name}"),
                    currentFile.readText()
                )
            }
        }

        newDir.deleteRecursively()
        oldDir.deleteRecursively()
    }

    internal fun browse(fileType: String, id: StorageId) {
        browse(fileType, id.i)
    }

    internal fun browse(fileType: String, id: Int) {
        val chooseFileIntent = Intent(Intent.ACTION_GET_CONTENT)
        chooseFileIntent.type = "application/$fileType"
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
        parentActivity.startActivityForResult(
            Intent.createChooser(chooseFileIntent, parentActivity.getString(R.string.settingsBackupChooseFile)), id
        )
    }

    private fun testFiles(): Boolean {
        return try {
            ShoppingListWrapper().check()
            BirthdayList(parentActivity.resources.getStringArray(R.array.months)).check()
            val noteDirList = NoteDirList()
            if (!noteDirList.loadedFromStorage) {
                throw IllegalStateException("Notes JSON could not be loaded")
            }
            noteDirList.check()

            TodoList().check()

            SettingsManager.init()
            SettingsManager.check()


            SleepReminder(parentActivity).check()
            UserItemTemplateList().check()

            Toast.makeText(parentActivity, parentActivity.getString(R.string.settingsBackupImportSuccessful), Toast.LENGTH_SHORT).show()

            true
        } catch (e: Throwable) {
            Log.e("IMPORT FAILED", e.toString())
            Toast.makeText(parentActivity, parentActivity.getString(R.string.settingsBackupImportFailed), Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun fallbackJson(id: StorageId): String {
        return when (id) {
            StorageId.SETTINGS -> "{}"
            else -> "[]"
        }
    }
}
