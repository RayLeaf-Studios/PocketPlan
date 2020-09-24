package com.pocket_plan.j7_003.data.settings

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import kotlinx.android.synthetic.main.fragment_settings.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsFr : Fragment() {
    lateinit var spNoteLines: Spinner
    lateinit var spNoteColumns: Spinner
    lateinit var spEditorFontSize: Spinner
    lateinit var spDrawerSide: Spinner
    lateinit var clManageCustomItems: ConstraintLayout
    lateinit var swExpandOneCategory: SwitchCompat
    lateinit var swCollapseCheckedSublists: SwitchCompat
    lateinit var swCloseItemDialog: SwitchCompat

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
        spEditorFontSize = myView.spEditorFontsize
        spDrawerSide = myView.spDrawerSide

        clManageCustomItems = myView.clManageCustomItems

        swExpandOneCategory = myView.swExpandOneCategory
        swCollapseCheckedSublists = myView.swCollapseCheckedSublists
        swCloseItemDialog = myView.swCloseAddItemDialog


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
        spEditorFontSize.adapter = spAdapterEditorFontSize

        //Spinner for drawer side
        val spAdapterDrawerSide = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.drawerSides)
        )
        spAdapterDrawerSide.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spDrawerSide.adapter = spAdapterDrawerSide
    }

    private fun initializeDisplayValues() {
        spNoteLines.setSelection(
            when (SettingsManager.getSetting(SettingId.NOTE_LINES)) {
                //0 = show no lines
                0.0 -> 1
                //n = show n lines
                1.0 -> 2
                3.0 -> 3
                5.0 -> 4
                10.0 -> 5
                20.0 -> 6
                //else case is -1 => show all lines
                else -> 0
            }
        )

        val columnOptions = resources.getStringArray(R.array.noteColumns)
        spNoteColumns.setSelection(columnOptions.indexOf(SettingsManager.getSetting(SettingId.NOTE_COLUMNS)))

        val fontSizeOptions = resources.getStringArray(R.array.fontSizes)
        spEditorFontSize.setSelection(fontSizeOptions.indexOf(SettingsManager.getSetting(SettingId.FONT_SIZE)))

        spDrawerSide.setSelection(
            when (SettingsManager.getSetting(SettingId.DRAWER_SIDE)) {
                //false = left side
                false -> 0
                //true = right side
                else -> 1
            }
        )

        swExpandOneCategory.isChecked =
            SettingsManager.getSetting(SettingId.EXPAND_ONE_CATEGORY) as Boolean
        swCollapseCheckedSublists.isChecked =
            SettingsManager.getSetting(SettingId.COLLAPSE_CHECKED_SUBLISTS) as Boolean

        swCloseItemDialog.isChecked =
            SettingsManager.getSetting(SettingId.CLOSE_ITEM_DIALOG) as Boolean
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
                val setTo = when(spNoteLines.selectedItemPosition){
                    0 -> -1.0
                    1 -> 0.0
                    2 -> 1.0
                    3 -> 3.0
                    4 -> 5.0
                    5 -> 10.0
                    else -> 20.0
                }
                SettingsManager.addSetting(SettingId.NOTE_LINES, setTo)
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
                SettingsManager.addSetting(SettingId.NOTE_COLUMNS, value)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        //Listener for note editor font size spinner
        spEditorFontSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val value = spEditorFontSize.selectedItem as String
                SettingsManager.addSetting(SettingId.FONT_SIZE, value)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        //Listener for drawerSide spinner
        spDrawerSide.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val setTo = when (spDrawerSide.selectedItemPosition) {
                    1 -> Pair(true, Gravity.END)
                    else -> Pair(false, Gravity.START)
                }
                SettingsManager.addSetting(SettingId.DRAWER_SIDE, setTo.first)

                MainActivity.drawerGravity = setTo.second
                MainActivity.params.gravity = setTo.second
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        //changing to custom item fragment via onclick listener
        clManageCustomItems.setOnClickListener {
            MainActivity.act.changeToFragment(FT.CUSTOM_ITEMS)
        }

        //Switch for only showing one category as expanded
        swExpandOneCategory.setOnClickListener {
            SettingsManager.addSetting(SettingId.EXPAND_ONE_CATEGORY, swExpandOneCategory.isChecked)
        }

        //Switch to collapse sublists when they are fully checked
        swCollapseCheckedSublists.setOnClickListener {
            SettingsManager.addSetting(
                SettingId.COLLAPSE_CHECKED_SUBLISTS,
                swCollapseCheckedSublists.isChecked
            )
        }

        //Switch to close item dialog after adding a single item
        swCloseItemDialog.setOnClickListener {
            SettingsManager.addSetting(
                SettingId.CLOSE_ITEM_DIALOG,
                swCloseItemDialog.isChecked
            )
        }
    }
}
