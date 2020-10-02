package com.pocket_plan.j7_003.system_interaction.handler.share

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pocket_plan.j7_003.R
import kotlinx.android.synthetic.main.fragment_settings_backup.*
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class BackUpActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val eHandler = ExportHandler()
        val iHandler = ImportHandler(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_settings_backup)
        btnConnect.text = "Export"
        btnSend.text = "Import"

        btnConnect.setOnClickListener {
            eHandler.shareAll()
        }

        btnDisconnect.setOnClickListener {
            finish()
        }

        btnSend.setOnClickListener {
            iHandler.import()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //this works to read a json
        val path = data?.data
        val newFile = File(filesDir.absolutePath + "/tmp.zip")

        val inputStream = contentResolver.openInputStream(path!!)!!
        val outputStream = newFile.outputStream()

        val byteArray = ByteArray(8)
        var length = inputStream.read(byteArray)

        while (length != -1) {
            outputStream.write(byteArray)
            length = inputStream.read(byteArray)
        }

        outputStream.close()
        inputStream.close()

        val read = ZipFile(newFile).getInputStream(ZipEntry("Settings.json")).bufferedReader().use { it.readText() }
        Log.e("here", read)
    }
}