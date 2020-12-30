package com.pocket_plan.j7_003.system_interaction.handler.share

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import kotlinx.android.synthetic.main.fragment_settings_backup.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File

class BackUpActivity: AppCompatActivity() {
    private val eHandler = ExportHandler(this)
    private val iHandler = ImportHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {


        val themeToSet = when(SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean){
            true -> R.style.AppThemeDark
            else -> R.style.AppThemeLight
        }
        setTheme(themeToSet)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_settings_backup)

        setSupportActionBar(myNewToolbar)

        //Spinner for single file export
        val spExportOneAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.fileOptions)
        )
        spExportOneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spExportOne.adapter = spExportOneAdapter

        //Spinner for single file import
        val spImportOneAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.fileOptions)
        )
        spImportOneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spImportOne.adapter = spImportOneAdapter

        //initialize spinners to show "shopping list" as default option
        spImportOne.setSelection(0)
        spExportOne.setSelection(0)

        initializeListeners()

    }

    private fun initializeListeners(){
        clExport.setOnClickListener {
            eHandler.shareAll()
        }

        clImport.setOnClickListener {
            iHandler.browse("zip", StorageId.ZIP)
        }

        tvExport.setOnClickListener {
            val storageId = StorageId.getByI(spExportOne.selectedItemPosition)

            if(storageId!=null){
                eHandler.shareById(storageId)
            }

        }

        tvImport.setOnClickListener {
            val storageId = StorageId.getByI(spImportOne.selectedItemPosition)

            if(storageId!=null){
                iHandler.browse("json", storageId)
            }

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

                    return
                }

                else -> {
                    val inputStream = contentResolver.openInputStream(data.data!!)!!
                    val targetId = StorageId.getByI(requestCode)

                    iHandler.importFromJson(targetId!!, inputStream, file)

                    inputStream.close()
                    zipFile.delete()
                    file.delete()

                    return
                }
            }
        } catch (e: Exception) {
            zipFile.delete()
            file.delete()
            return
        }
    }
}