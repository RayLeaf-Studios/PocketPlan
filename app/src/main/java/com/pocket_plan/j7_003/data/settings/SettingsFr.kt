package com.pocket_plan.j7_003.data.settings

import android.content.Intent
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
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsFr : Fragment() {
    lateinit var spNoteLines: Spinner
    lateinit var spNoteColumns: Spinner
    lateinit var spEditorFontsize: Spinner
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
        initializeAdapters()
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
        spEditorFontsize = myView.spEditorFontsize

    }

    private fun initializeAdapters(){
        //NOTES
        //Spinner for amount of noteLines to be displayed
        val spAdapterNoteLines = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.noteLines)
        )
        spAdapterNoteLines.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNoteLines.adapter = spAdapterNoteLines

        //Spinner for amount of note columns
        val spAdapterNoteColumns = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.noteColumns)
        )
        spAdapterNoteColumns.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNoteColumns.adapter = spAdapterNoteColumns

        //Spinner for amount of note columns
        val spAdapterEditorFontSize = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.fontSizes)
        )
        spAdapterEditorFontSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spEditorFontsize.adapter = spAdapterEditorFontSize
    }

    private fun initializeDisplayValues() {
        val lineOptions = resources.getStringArray(R.array.noteLines)
        spNoteLines.setSelection(lineOptions.indexOf(SettingsManager.getSetting("noteLines")))

        val columnOptions = resources.getStringArray(R.array.noteColumns)
        spNoteColumns.setSelection(columnOptions.indexOf(SettingsManager.getSetting("noteColumns")))

        val fontSizeOptions = resources.getStringArray(R.array.fontSizes)
        spEditorFontsize.setSelection(fontSizeOptions.indexOf(SettingsManager.getSetting("fontSize")))

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

        //Listener for note editor font size spinner
        spEditorFontsize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val value = spEditorFontsize.selectedItem as String
                SettingsManager.addSetting("fontSize", value)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        //Switch for only showing one category as expanded
        clManageCustomItems.setOnClickListener {
            MainActivity.act.changeToFragment(FT.CUSTOM_ITEMS)
        }

        swExpandOneCategory.setOnClickListener {
            SettingsManager.addSetting("expandOneCategory", swExpandOneCategory.isChecked)
        }

        swCollapseCheckedSublists.setOnClickListener {
            SettingsManager.addSetting("collapseCheckedSublists", swCollapseCheckedSublists.isChecked)
        }

        swLeftHanded.setOnClickListener {
            SettingsManager.addSetting("drawerLeftSide", swLeftHanded.isChecked)
            MainActivity.act.finish()
            val launchIntent = Intent(activity, MainActivity::class.java)
            launchIntent.putExtra("NotificationEntry", "settings")
            startActivity(launchIntent)
        }
    }
}
