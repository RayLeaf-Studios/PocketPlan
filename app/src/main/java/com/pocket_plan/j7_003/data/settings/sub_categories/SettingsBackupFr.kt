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
    private lateinit var spImportOne: Spinner
    private lateinit var spExportOne: Spinner

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
        //Spinner for single export
        val spExportOneAdapter = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.fileOptions)
        )
        spExportOneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spExportOne.adapter = spExportOneAdapter

        //Spinner for single import
        val spImportOneAdapter = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.fileOptions)
        )
        spImportOneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spImportOne.adapter = spImportOneAdapter

    }

    private fun initializeDisplayValues() {
        spExportOne.setSelection(0)
        spImportOne.setSelection(0)
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
