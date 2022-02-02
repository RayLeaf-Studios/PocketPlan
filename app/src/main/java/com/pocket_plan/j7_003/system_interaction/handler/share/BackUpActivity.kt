package com.pocket_plan.j7_003.system_interaction.handler.share

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.data.sleepreminder.SleepReminder
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import kotlinx.android.synthetic.main.fragment_settings_backup.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File

/**
 * A simple activity used to handle the backup process of the app.
 */
class BackUpActivity : AppCompatActivity() {
    private val eHandler = ExportHandler(this)
    private val iHandler = ImportHandler(this)


    /**
     * Called at creation of the activity and handles the displayed buttons,
     * text and listeners for the logic.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        val themeToSet = when (SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean) {
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

        // adds the logic to the export/import buttons
        initializeListeners()
    }

    private fun initializeListeners() {
        clExport.setOnClickListener {
            eHandler.shareAll()
        }

        clImport.setOnClickListener {
            iHandler.browse("zip", StorageId.ZIP)
        }

        tvExport.setOnClickListener {
            val storageId = StorageId.getByI(spExportOne.selectedItemPosition)

            if (storageId != null) {
                eHandler.shareById(storageId)
            }

        }

        tvImport.setOnClickListener {
            val storageId = StorageId.getByI(spImportOne.selectedItemPosition)

            if (storageId != null) {
                iHandler.browse("json", storageId)
            }

        }

    }

    /**
     * Called when the file picker activity (from the ImportHandler) returns.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // if no file was selected or the picking was interrupted/cancelled
        if (data == null) {
            return
        }

        // creation of used file objects for the different types of import
        val zipFile = File("$filesDir/newBundle.zip")
        val file = File("$filesDir/file_from_backup.tmp")

        try {
            when (requestCode) {
                StorageId.ZIP.i -> {// run if the request code corresponds to the storage id of the zip file
                    // input stream of the picked file/the file to read from
                    // is closed by the import handler
                    val inputStream = contentResolver.openInputStream(data.data!!)!!

                    // actual import process
                    iHandler.importFromZip(inputStream, zipFile)

                    // removing now not needed files
                    zipFile.delete()
                    file.delete()
                }

                else -> {
                    // input stream of the picked file/the file to read from
                    // is closed by the import handler
                    val inputStream = contentResolver.openInputStream(data.data!!)!!
                    // getting the storage id of the requested file by the request code
                    val targetId = StorageId.getByI(requestCode)

                    // actual import process
                    iHandler.importFromJson(targetId!!, inputStream, file)

                    // removing now not needed files
                    zipFile.delete()
                    file.delete()

                    if (targetId == StorageId.SLEEP) {
                        SleepReminder(this).updateReminder()
                    }
                }
            }
            startMainActivity()
            this.finish()
        } catch (e: Exception) {    // in case something goes wrong during the import process
            zipFile.delete()
            file.delete()
            Toast.makeText(baseContext, "Import failed!", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun startMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("NotificationEntry", "backup")
        startActivity(intent)
    }

    override fun onBackPressed() {
        startMainActivity()
        this.finish()
        super.onBackPressed()
    }
}