package com.pocket_plan.j7_003.system_interaction.handler.share

import android.content.Intent
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import kotlinx.android.synthetic.main.fragment_settings_backup.*
import kotlinx.android.synthetic.main.new_app_bar.*
import java.io.File

class BackUpActivity: AppCompatActivity() {
    private val eHandler = ExportHandler()
    private val iHandler = ImportHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_settings_backup)

        setSupportActionBar(myNewToolbar)
        clExport.setOnClickListener {
            eHandler.shareAll()
        }

        eHandler.backUpAsZip()

        clImport.setOnClickListener {
            iHandler.browse("zip", StorageId.ZIP)
        }

        clImport.setOnLongClickListener {
            iHandler.browse("json", StorageId.BIRTHDAYS)
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }

        val zipFile = File("$filesDir/newBundle.zip")
        val file = File("$filesDir/file_from_backup.tmp")

        try {
            when (requestCode) {
                StorageId.ZIP.i -> {
                    val inputStream = contentResolver.openInputStream(data.data!!)!!

                    iHandler.importFromZip(inputStream, zipFile)
                    zipFile.delete()
                    file.delete()

                    MainActivity.act.refreshData()

                    return
                }

                else -> {
                    val inputStream = contentResolver.openInputStream(data.data!!)!!
                    val targetId = StorageId.getByI(requestCode)

                    iHandler.importFromJson(targetId!!, inputStream, file)

                    inputStream.close()
                    zipFile.delete()
                    file.delete()

                    MainActivity.act.refreshData()

                    return
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Couldn't import!", Toast.LENGTH_SHORT).show()
            zipFile.delete()
            file.delete()
            Log.e("e", e.toString())
            return
        }
    }
}