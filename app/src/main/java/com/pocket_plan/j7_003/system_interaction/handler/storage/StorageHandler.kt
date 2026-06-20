package com.pocket_plan.j7_003.system_interaction.handler.storage

import com.google.gson.Gson
import com.google.gson.JsonParser
import android.util.AtomicFile
import java.io.File
import java.io.FileOutputStream

class StorageHandler {

    companion object {
        private const val BACKUP_COUNT = 3

        var files = HashMap<StorageId, File>()
        lateinit var path: String

        fun saveAsJsonToFile(file: File?, any: Any) {
            val json = Gson().toJson(any)
            if (!isValidJson(json)) {
                throw IllegalArgumentException("Refusing to save invalid JSON")
            }
            writeTextToFile(file, json)
        }

        fun writeTextToFile(file: File?, text: String, rotateBackup: Boolean = true) {
            if (file == null) return
            if (rotateBackup) rotateBackups(file)
            writeTextAtomic(file, text)
        }

        fun readJsonFromFile(file: File?, fallbackText: String? = null): String? {
            if (file == null) return fallbackText

            readValidJson(file)?.let { return it }

            if (file.exists()) {
                preserveCorruptFile(file)
            }

            for (i in 1..BACKUP_COUNT) {
                val backupText = readValidJson(backupFile(file, i)) ?: continue
                writeTextAtomic(file, backupText)
                return backupText
            }

            if (fallbackText != null) {
                if (!isValidJson(fallbackText)) {
                    throw IllegalArgumentException("Refusing to use invalid fallback JSON")
                }
                writeTextToFile(file, fallbackText, rotateBackup = false)
                return fallbackText
            }

            throw IllegalStateException("No readable JSON storage file found for ${file.name}")
        }

        fun createFile(identifier: StorageId, fileName: String) {
            files[identifier] =
                setStorageLocation(fileName)

            if (files[identifier]?.exists() == null || files[identifier]?.exists() == false) {
                files[identifier]?.createNewFile()
            }
        }

        fun createJsonFile(identifier: StorageId, text: String = "[]") {
            files[identifier] = setStorageLocation(identifier.s)

            if (files[identifier]?.exists() == null || files[identifier]?.exists() == false) {
                writeTextToFile(files[identifier], text, rotateBackup = false)
            }
        }

        private fun setStorageLocation(fileName: String): File =
            File(path, fileName)

        private fun rotateBackups(file: File) {
            if (!file.exists()) return

            val currentText = try {
                file.readText()
            } catch (_: Exception) {
                return
            }

            if (!isValidJson(currentText)) {
                preserveCorruptFile(file)
                return
            }

            for (i in BACKUP_COUNT downTo 2) {
                val previous = backupFile(file, i - 1)
                if (previous.exists()) {
                    writeTextAtomic(backupFile(file, i), previous.readText())
                }
            }
            writeTextAtomic(backupFile(file, 1), currentText)
        }

        private fun readValidJson(file: File): String? {
            if (!file.exists()) return null
            val text = try {
                file.readText()
            } catch (_: Exception) {
                return null
            }
            return if (isValidJson(text)) text else null
        }

        private fun isValidJson(text: String): Boolean {
            if (text.isBlank()) return false
            return try {
                JsonParser.parseString(text)
                true
            } catch (_: Exception) {
                false
            }
        }

        private fun preserveCorruptFile(file: File) {
            if (!file.exists()) return
            val corruptText = try {
                file.readText()
            } catch (_: Exception) {
                return
            }
            val corruptFile = File(file.parentFile, "${file.name}.corrupt")
            if (!corruptFile.exists()) {
                writeTextAtomic(corruptFile, corruptText)
            }
        }

        private fun backupFile(file: File, index: Int): File =
            File(file.parentFile, "${file.name}.bak$index")

        private fun writeTextAtomic(file: File, text: String) {
            val atomicFile = AtomicFile(file)
            var stream: FileOutputStream? = null
            try {
                val outputStream = atomicFile.startWrite()
                stream = outputStream
                outputStream.write(text.toByteArray(Charsets.UTF_8))
                atomicFile.finishWrite(outputStream)
                stream = null
            } catch (e: Exception) {
                stream?.let { atomicFile.failWrite(it) }
                throw e
            }
        }

    }
}
