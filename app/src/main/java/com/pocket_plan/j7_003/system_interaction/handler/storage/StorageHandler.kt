package com.pocket_plan.j7_003.system_interaction.handler.storage

import com.google.gson.Gson
import java.io.File

class StorageHandler {

    companion object{
        var files = HashMap<StorageId, File>()
        lateinit var path: String

        fun saveAsJsonToFile(file: File?, any: Any) = file?.writeText(Gson().toJson(any))

        fun createFile(identifier: StorageId, fileName: String) {
            files[identifier] =
                setStorageLocation(fileName)

            if (files[identifier]?.exists() == null || files[identifier]?.exists() == false) {
                files[identifier]?.createNewFile()
            }
        }

        fun createJsonFile(identifier: StorageId, text: String = "[]") {
            files[identifier] =
                setStorageLocation(identifier.s)

            if (files[identifier]?.exists() == null || files[identifier]?.exists() == false) {
                files[identifier]?.writeText(text)
            }
        }

        private fun setStorageLocation(fileName: String): File =
            File(path, fileName)

    }
}