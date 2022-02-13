package com.pocket_plan.j7_003.data.settings.sub_categories

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
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

    private var initialDisplayNoteLines: Boolean = true
    private var initialDisplayNoteColumns: Boolean = true
    private var initialDisplayFontSize: Boolean = true

    private lateinit var swAllowSwipe: SwitchCompat
    private lateinit var swRandomizeNoteColors: SwitchCompat
    private lateinit var swShowContained: SwitchCompat
    private lateinit var swMoveUpCurrentNote: SwitchCompat
    private lateinit var swArchive: SwitchCompat

    private lateinit var clNoteLines: ConstraintLayout
    private lateinit var clNoteColumns: ConstraintLayout
    private lateinit var clFontSize: ConstraintLayout
    private lateinit var clShowArchive: ConstraintLayout
    private lateinit var clClearArchive: ConstraintLayout

    private lateinit var tvCurrentNoteLines: TextView
    private lateinit var tvCurrentNoteColumns: TextView
    private lateinit var tvCurrentFontSize: TextView
    private lateinit var tvArchive: TextView

    private lateinit var ivArchiveExpand: ImageView
    private lateinit var svArchive: NestedScrollView

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
        swShowContained = myView.swShowContained
        swMoveUpCurrentNote = myView.swMoveUpCurrentNote
        swArchive = myView.swArchive

        clNoteColumns = myView.clNoteColumns
        clNoteLines = myView.clNoteLines
        clFontSize = myView.clFontSize

        tvCurrentNoteLines = myView.tvCurrentNoteLines
        tvCurrentNoteColumns = myView.tvCurrentNoteColumns
        tvCurrentFontSize = myView.tvCurrentNoteEditorFontSize
        tvArchive = myView.tvArchive

        ivArchiveExpand = myView.ivArchiveExpand
        svArchive = myView.svArchive
        clShowArchive = myView.clShowArchive
        clClearArchive = myView.clClearArchive
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
        val noteLinesStringIndex = when (SettingsManager.getSetting(SettingId.NOTE_LINES)) {
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
        spNoteLines.setSelection(noteLinesStringIndex)
        tvCurrentNoteLines.text = resources.getStringArray(R.array.noteLines)[noteLinesStringIndex]

        val columnOptions = resources.getStringArray(R.array.noteColumns)
        val columnOptionsStringIndex = columnOptions.indexOf(SettingsManager.getSetting(SettingId.NOTE_COLUMNS))
        spNoteColumns.setSelection(columnOptionsStringIndex)
        tvCurrentNoteColumns.text = columnOptions[columnOptionsStringIndex]

        val fontSizeOptions = resources.getStringArray(R.array.fontSizes)
        val fontSizeOptionsStringIndex = fontSizeOptions.indexOf(SettingsManager.getSetting(SettingId.FONT_SIZE))
        spEditorFontSize.setSelection(fontSizeOptionsStringIndex)
        tvCurrentFontSize.text = fontSizeOptions[fontSizeOptionsStringIndex]

        swAllowSwipe.isChecked = SettingsManager.getSetting(SettingId.NOTES_SWIPE_DELETE) as Boolean
        swRandomizeNoteColors.isChecked = SettingsManager.getSetting(SettingId.RANDOMIZE_NOTE_COLORS) as Boolean
        swShowContained.isChecked = SettingsManager.getSetting(SettingId.NOTES_SHOW_CONTAINED) as Boolean
        swMoveUpCurrentNote.isChecked = SettingsManager.getSetting(SettingId.NOTES_MOVE_UP_CURRENT) as Boolean
        swArchive.isChecked = SettingsManager.getSetting(SettingId.NOTES_ARCHIVE) as Boolean

        val archiveContent = PreferenceManager.getDefaultSharedPreferences(myActivity).getString("noteArchive", "")
        if (archiveContent != null) {
            tvArchive.text = when(archiveContent.trim()==""){
                true -> {
                    "No recently deleted notes"
                }
                else -> {
                    archiveContent
                }
            }
        }
        svArchive.visibility = View.GONE
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
                if(initialDisplayNoteLines){
                    initialDisplayNoteLines = false
                    return
                }
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
                if(initialDisplayNoteColumns){
                    initialDisplayNoteColumns = false
                    return
                }
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
                if(initialDisplayFontSize){
                    initialDisplayFontSize = false
                    return
                }
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

        swShowContained.setOnClickListener{
            SettingsManager.addSetting(SettingId.NOTES_SHOW_CONTAINED, swShowContained.isChecked)
        }

        swMoveUpCurrentNote.setOnClickListener{
            SettingsManager.addSetting(SettingId.NOTES_MOVE_UP_CURRENT, swMoveUpCurrentNote.isChecked)
        }

        swArchive.setOnClickListener{
            SettingsManager.addSetting(SettingId.NOTES_ARCHIVE, swArchive.isChecked)
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

        clShowArchive.setOnClickListener {
            if(svArchive.visibility == View.VISIBLE){
                svArchive.visibility = View.GONE
                ivArchiveExpand.rotation = 0f
            }else{
                svArchive.visibility = View.VISIBLE
                ivArchiveExpand.rotation = 180f
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    svNotesSettings.scrollToDescendant(svArchive)
                }else{
                //Todo proper scroll behavior for version < Q
                    svArchive.fullScroll(ScrollView.FOCUS_DOWN)
                }
            }

        }

        clClearArchive.setOnClickListener {
            val action: () -> Unit = {
                PreferenceManager.getDefaultSharedPreferences(myActivity).edit().putString("noteArchive", "").apply()
                tvArchive.text = getString(R.string.no_archived_notes)
                ivArchiveExpand.rotation = 0f
                svArchive.visibility = View.GONE
            }
            myActivity.dialogConfirm(getString(R.string.dialog_clear_archive), action)
        }
    }
}
