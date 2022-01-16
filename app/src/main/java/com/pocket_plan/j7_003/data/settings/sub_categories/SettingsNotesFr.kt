package com.pocket_plan.j7_003.data.settings.sub_categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.fragment_settings_notes.*
import kotlinx.android.synthetic.main.fragment_settings_notes.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsNotesFr : Fragment() {
    lateinit var myActivity: MainActivity
    lateinit var spNoteLines: Spinner
    lateinit var spNoteColumns: Spinner
    lateinit var spEditorFontSize: Spinner
    private lateinit var swAllowSwipe: SwitchCompat
    private lateinit var swRandomizeNoteColors: SwitchCompat

    private lateinit var clNoteLines: ConstraintLayout
    private lateinit var clNoteColumns: ConstraintLayout
    private lateinit var clFontSize: ConstraintLayout

    private lateinit var tvCurrentNoteLines: TextView
    private lateinit var tvCurrentNoteColumns: TextView
    private lateinit var tvCurrentFontSize: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myActivity = activity as MainActivity
        val myView = inflater.inflate(R.layout.fragment_settings_notes, container, false)

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
        swAllowSwipe = myView.swAllowSwipe
        swRandomizeNoteColors = myView.swRandomizeColors

        clNoteColumns = myView.clNoteColumns
        clNoteLines = myView.clNoteLines
        clFontSize = myView.clFontSize

        tvCurrentNoteLines = myView.tvCurrentNoteLines
        tvCurrentNoteColumns = myView.tvCurrentNoteColumns
        tvCurrentFontSize = myView.tvCurrentNoteEditorFontSize
    }

    private fun initializeAdapters() {
        //NOTES
        //Spinner for amount of noteLines to be displayed
        val spAdapterNoteLines = ArrayAdapter(
            myActivity,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.noteLines)
        )
        spAdapterNoteLines.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNoteLines.adapter = spAdapterNoteLines

        //Spinner for amount of note columns
        val spAdapterNoteColumns = ArrayAdapter(
            myActivity,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.noteColumns)
        )
        spAdapterNoteColumns.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNoteColumns.adapter = spAdapterNoteColumns

        //Spinner for amount of note columns
        val spAdapterEditorFontSize = ArrayAdapter(
            myActivity,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.fontSizes)
        )
        spAdapterEditorFontSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spEditorFontSize.adapter = spAdapterEditorFontSize

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
        tvCurrentNoteLines.text = resources.getStringArray(R.array.noteLines)[spNoteLines.selectedItemPosition]

        val columnOptions = resources.getStringArray(R.array.noteColumns)
        spNoteColumns.setSelection(columnOptions.indexOf(SettingsManager.getSetting(SettingId.NOTE_COLUMNS)))

        val fontSizeOptions = resources.getStringArray(R.array.fontSizes)
        spEditorFontSize.setSelection(fontSizeOptions.indexOf(SettingsManager.getSetting(SettingId.FONT_SIZE)))

        swAllowSwipe.isChecked = SettingsManager.getSetting(SettingId.NOTES_SWIPE_DELETE) as Boolean

        swRandomizeNoteColors.isChecked = SettingsManager.getSetting(SettingId.RANDOMIZE_NOTE_COLORS) as Boolean
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
                tvCurrentNoteLines.text = resources.getStringArray(R.array.noteLines)[position]
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
                tvCurrentNoteColumns.text = value
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
                tvCurrentFontSize.text = value
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        //listener for switch to allow / disallow swipe to delete for notes
        swAllowSwipe.setOnClickListener {
            SettingsManager.addSetting(SettingId.NOTES_SWIPE_DELETE, swAllowSwipe.isChecked)
        }

        swRandomizeNoteColors.setOnClickListener{
            SettingsManager.addSetting(SettingId.RANDOMIZE_NOTE_COLORS, swRandomizeNoteColors.isChecked)
        }

        clNoteLines.setOnClickListener {
            spNoteLines.performClick()
        }

        clNoteColumns.setOnClickListener {
            spNoteColumns.performClick()
        }

        clFontSize.setOnClickListener {
            spEditorFontSize.performClick()
        }
    }
}
