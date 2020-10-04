package com.pocket_plan.j7_003.system_interaction.handler.share

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
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
            iHandler.browse("zip", 2000)
        }

        clImport.setOnLongClickListener {
            iHandler.browse("json", 2001)
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        //this works to read a json
        when (requestCode) {
            2000 -> {
                val inputStream = contentResolver.openInputStream(data?.data!!)!!
                val zipFile = File("$filesDir/newBundle.zip")

                iHandler.importFromZip(inputStream, zipFile)
                zipFile.delete()

                MainActivity.act.refreshData()

                return
            }

            2001 -> { iHandler.importFromJson(); return }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}