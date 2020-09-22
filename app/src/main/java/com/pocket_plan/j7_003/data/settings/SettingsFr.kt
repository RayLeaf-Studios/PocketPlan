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
    lateinit var spEditorFontsize: Spinner
    lateinit var spDrawerSide: Spinner
    private lateinit var clManageCustomItems: ConstraintLayout
    private lateinit var swExpandOneCategory: SwitchCompat
    private lateinit var swCollapseCheckedSublists: SwitchCompat

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
        spEditorFontsize = myView.spEditorFontsize
        spDrawerSide = myView.spDrawerSide

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
        spEditorFontsize.adapter = spAdapterEditorFontSize

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
        val lineOptions = resources.getStringArray(R.array.noteLines)
        spNoteLines.setSelection(lineOptions.indexOf(SettingsManager.getSetting(SettingId.NOTE_LINES)))

        val columnOptions = resources.getStringArray(R.array.noteColumns)
        spNoteColumns.setSelection(columnOptions.indexOf(SettingsManager.getSetting(SettingId.NOTE_COLUMNS)))

        val fontSizeOptions = resources.getStringArray(R.array.fontSizes)
        spEditorFontsize.setSelection(fontSizeOptions.indexOf(SettingsManager.getSetting(SettingId.FONT_SIZE)))

        val drawerSideOptions = resources.getStringArray(R.array.drawerSides)
        spDrawerSide.setSelection(drawerSideOptions.indexOf(SettingsManager.getSetting(SettingId.DRAWER_SIDE)))

        swExpandOneCategory.isChecked = SettingsManager.getSetting(SettingId.EXPAND_ONE_CATEGORY) as Boolean
        swCollapseCheckedSublists.isChecked =
            SettingsManager.getSetting(SettingId.COLLAPSE_CHECKED_SUBLISTS) as Boolean

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
                SettingsManager.addSetting(SettingId.NOTE_LINES, value)
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
        spEditorFontsize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val value = spEditorFontsize.selectedItem as String
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
                val value = spDrawerSide.selectedItem as String
                SettingsManager.addSetting(SettingId.DRAWER_SIDE, value)

                val newGravity =  when (value) {
                    resources.getStringArray(R.array.drawerSides)[1] -> Gravity.END
                    else -> Gravity.START
                }

                MainActivity.drawerGravity = newGravity
                MainActivity.params.gravity = newGravity
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        //Switch for only showing one category as expanded
        clManageCustomItems.setOnClickListener {
            MainActivity.act.changeToFragment(FT.CUSTOM_ITEMS)
        }

        swExpandOneCategory.setOnClickListener {
            SettingsManager.addSetting(SettingId.EXPAND_ONE_CATEGORY, swExpandOneCategory.isChecked)
        }

        swCollapseCheckedSublists.setOnClickListener {
            SettingsManager.addSetting(
                SettingId.COLLAPSE_CHECKED_SUBLISTS,
                swCollapseCheckedSublists.isChecked
            )
        }

    }
}
