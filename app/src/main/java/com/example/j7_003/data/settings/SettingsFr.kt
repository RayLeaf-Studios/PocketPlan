package com.example.j7_003.data.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsFr : Fragment() {
    lateinit var spNoteLines: Spinner
    lateinit var spNoteColumns: Spinner
    private lateinit var clManageCustomItems: ConstraintLayout
    private lateinit var swExpandOneCategory: Switch
    private lateinit var swCollapseCheckedSublists: Switch
    private lateinit var swLeftHanded: Switch

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_settings, container, false)

        initializeComponents(myView)
        initializeDisplayValues()
        initializeListeners()

        return myView
    }

    private fun initializeComponents(myView: View) {

        //initialize references to view
        spNoteLines = myView.spNoteLines
        spNoteColumns = myView.spNoteColumns
        clManageCustomItems = myView.clManageCustomItems
        swExpandOneCategory = myView.swExpandOneCategory
        swCollapseCheckedSublists = myView.swCollapseCheckedSublists
        swLeftHanded = myView.swLeftHanded

        /**
         * INITIALIZE ADAPTERS
         */

        //NOTES
        //Spinner for amount of noteLines to be displayed
        val spAdapterNoteLines = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.noteLines)
        )
        spAdapterNoteLines.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNoteLines.adapter = spAdapterNoteLines

        //SHOPPING-LIST
        //Spinner for amount of note columns
        val spAdapterNoteColumns = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.noteColumns)
        )
        spAdapterNoteColumns.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNoteColumns.adapter = spAdapterNoteColumns
    }

    private fun initializeDisplayValues() {
        //TODO read the following 4 values from settings manager
        val lineOptions = resources.getStringArray(R.array.noteLines)
        spNoteLines.setSelection(lineOptions.indexOf(SettingsManager.getSetting("noteLines")))

        val columnOptions = resources.getStringArray(R.array.noteColumns)
        spNoteColumns.setSelection(columnOptions.indexOf(SettingsManager.getSetting("noteColumns")))

        swExpandOneCategory.isChecked = SettingsManager.getSetting("expandOneCategory") as Boolean
        swCollapseCheckedSublists.isChecked = SettingsManager.getSetting("collapseCheckedSublists") as Boolean
        swLeftHanded.isChecked = SettingsManager.getSetting("drawerLeftSide") as Boolean

    }

    private fun initializeListeners() {
        //Listener for note line amount spinner
        spNoteLines.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val value = spNoteLines.selectedItem as String
                SettingsManager.addSetting("noteLines", value)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        //Listener for note column amount spinner
        spNoteColumns.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val value = spNoteColumns.selectedItem as String
                SettingsManager.addSetting("noteColumns", value)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        //Switch for only showing one category as expanded
        clManageCustomItems.setOnClickListener {
            MainActivity.act.changeToCustomItems()
        }

        swExpandOneCategory.setOnClickListener {
            SettingsManager.addSetting("expandOneCategory", swExpandOneCategory.isChecked)
        }

        swCollapseCheckedSublists.setOnClickListener {
            SettingsManager.addSetting("collapseCheckedSublists", swCollapseCheckedSublists.isChecked)
        }

        swLeftHanded.setOnClickListener {
            SettingsManager.addSetting("drawerLeftSide", swCollapseCheckedSublists.isChecked)
        }

    }
}
