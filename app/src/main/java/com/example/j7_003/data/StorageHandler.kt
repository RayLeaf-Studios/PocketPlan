package com.example.j7_003.data

import android.content.Context
import android.os.Build
import android.os.Environment
import com.google.gson.Gson
import java.io.File

class StorageHandler(val context: Context) {

    companion object {
        val files = HashMap<String, File>()
    }

    fun saveToFile(file: File?, any: Any) = file?.writeText(Gson().toJson(any))

    fun addListToFiles(identifier: String, fileName: String) {
        files[identifier] = setStorageLocation(fileName, context)

        if (files[identifier]?.exists() == null || files[identifier]?.exists() == false) {
            files[identifier]?.writeText("[]")
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