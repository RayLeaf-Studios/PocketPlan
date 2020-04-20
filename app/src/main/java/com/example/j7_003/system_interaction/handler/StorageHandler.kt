package com.example.j7_003.system_interaction.handler

import android.content.Context
import android.os.Build
import android.os.Environment
import com.example.j7_003.MainActivity
import com.google.gson.Gson
import java.io.File

class StorageHandler {

    companion object {
        val files = HashMap<String, File>()

        fun saveAsJsonToFile(file: File?, any: Any) = file?.writeText(Gson().toJson(any))

        fun createFile(identifier: String, fileName: String) {
            files[identifier] =
                setStorageLocation(
                    fileName,
                    MainActivity.myActivity
                )

            if (files[identifier]?.exists() == null || files[identifier]?.exists() == false) {
                files[identifier]?.createNewFile()
            }
        }

        fun createJsonFile(
            identifier: String,
            fileName: String,
            context: Context = MainActivity.myActivity,
            text: String = "[]"
        ) {
            files[identifier] =
                setStorageLocation(
                    fileName,
                    context
                )

            if (files[identifier]?.exists() == null || files[identifier]?.exists() == false) {
                files[identifier]?.writeText(text)
            }
        }

        private fun setStorageLocation(fileName: String, context: Context): File {
            return if (Build.VERSION.SDK_INT < 29) {
                File("${Environment.getDataDirectory()}/data/com.example.j7_003", fileName)
            } else {
                File(context.filesDir, fileName)
            }
        }
    }
}