package com.pocket_plan.j7_003.data.settings.sub_categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.fragment_settings_backup.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsBackupFr : Fragment() {
    lateinit var spImportOne: Spinner
    lateinit var spExportOne: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_settings_backup, container, false)

        initializeComponents(myView)
        initializeAdapters()
        initializeDisplayValues()
        initializeListeners()

        return myView
    }

    private fun initializeComponents(myView: View) {

        //initialize references to view
        spExportOne = myView.spExportOne
        spImportOne = myView.spImportOne
    }

    private fun initializeAdapters() {
        //NOTES
        //Spinner for amount of noteLines to be displayed
        val spAdapterNoteLines = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.noteLines)
        )
        spAdapterNoteLines.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        //Spinner for amount of note columns
        val spAdapterNoteColumns = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.noteColumns)
        )
        spAdapterNoteColumns.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spExportOne.adapter = spAdapterNoteColumns

        //Spinner for amount of note columns
        val spAdapterEditorFontSize = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.fontSizes)
        )
        spAdapterEditorFontSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    }

    private fun initializeDisplayValues() {
        val columnOptions = resources.getStringArray(R.array.noteColumns)
        spExportOne.setSelection(columnOptions.indexOf(SettingsManager.getSetting(SettingId.NOTE_COLUMNS)))

        val fontSizeOptions = resources.getStringArray(R.array.fontSizes)
    }

    private fun initializeListeners() {
        //Listener for note column amount spinner
        spExportOne.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val value = spExportOne.selectedItem as String
                SettingsManager.addSetting(SettingId.NOTE_COLUMNS, value)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }
}
