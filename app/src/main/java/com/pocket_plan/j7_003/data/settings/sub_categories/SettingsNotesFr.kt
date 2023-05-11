package com.pocket_plan.j7_003.data.settings.sub_categories

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.databinding.FragmentSettingsNotesBinding

/**
 * A simple [Fragment] subclass.
 */
class SettingsNotesFr : Fragment() {
    private var _fragmentBinding: FragmentSettingsNotesBinding? = null
    private val fragmentBinding get() = _fragmentBinding!!

    lateinit var myActivity: MainActivity

    private var initialDisplayNoteLines: Boolean = true
    private var initialDisplayNoteColumns: Boolean = true
    private var initialDisplayFontSize: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentBinding = FragmentSettingsNotesBinding.inflate(inflater, container, false)
        myActivity = activity as MainActivity

        initializeAdapters()
        initializeDisplayValues()
        initializeListeners()

        return fragmentBinding.root
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
        fragmentBinding.spNoteLines.adapter = spAdapterNoteLines

        //Spinner for amount of note columns
        val spAdapterNoteColumns = ArrayAdapter(
            myActivity,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.noteColumns)
        )
        spAdapterNoteColumns.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fragmentBinding.spNoteColumns.adapter = spAdapterNoteColumns

        //Spinner for amount of note columns
        val spAdapterEditorFontSize = ArrayAdapter(
            myActivity,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.fontSizes)
        )
        spAdapterEditorFontSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fragmentBinding.spEditorFontSize.adapter = spAdapterEditorFontSize

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
        fragmentBinding.spNoteLines.setSelection(noteLinesStringIndex)
        fragmentBinding.tvCurrentNoteLines.text = resources.getStringArray(R.array.noteLines)[noteLinesStringIndex]

        val columnIndex = (SettingsManager.getSetting(SettingId.NOTE_COLUMNS) as String).trim().toInt() - 1
        fragmentBinding.spNoteColumns.setSelection(columnIndex)

        val columnOptions = resources.getStringArray(R.array.noteColumns)
        fragmentBinding.tvCurrentNoteColumns.text = columnOptions[columnIndex]

        val fontSizeOptions = resources.getStringArray(R.array.fontSizes)
        fontSizeOptions.forEachIndexed {i, it ->
            fontSizeOptions[i] = it.trim()
        }
        val fontSizeOptionsStringIndex = fontSizeOptions.indexOf(SettingsManager.getSetting(SettingId.FONT_SIZE).toString().trim())
        fragmentBinding.spEditorFontSize.setSelection(fontSizeOptionsStringIndex)
        fragmentBinding.tvCurrentNoteEditorFontSize.text = fontSizeOptions[fontSizeOptionsStringIndex]
        fragmentBinding.tvEditorSample.textSize = fontSizeOptions[fontSizeOptionsStringIndex].toFloat()

        fragmentBinding.swAllowSwipe.isChecked = SettingsManager.getSetting(SettingId.NOTES_SWIPE_DELETE) as Boolean
        fragmentBinding.swRandomizeNoteColors.isChecked = SettingsManager.getSetting(SettingId.RANDOMIZE_NOTE_COLORS) as Boolean
        fragmentBinding.swShowContained.isChecked = SettingsManager.getSetting(SettingId.NOTES_SHOW_CONTAINED) as Boolean
        fragmentBinding.swMoveUpCurrentNote.isChecked = SettingsManager.getSetting(SettingId.NOTES_MOVE_UP_CURRENT) as Boolean
        fragmentBinding.swArchive.isChecked = SettingsManager.getSetting(SettingId.NOTES_ARCHIVE) as Boolean
        fragmentBinding.swFixedNoteSize.isChecked = SettingsManager.getSetting(SettingId.NOTES_FIXED_SIZE) as Boolean
        fragmentBinding.swSortFoldersToTop.isChecked = SettingsManager.getSetting(SettingId.NOTES_DIRS_TO_TOP) as Boolean

        fragmentBinding.clNoteLines.visibility = when(fragmentBinding.swFixedNoteSize.isChecked){
            true -> View.GONE
            else -> View.VISIBLE
        }

        val archiveContent = PreferenceManager.getDefaultSharedPreferences(myActivity).getString("noteArchive", "")
        if (archiveContent != null) {
            fragmentBinding.tvArchive.text = when(archiveContent.trim()==""){
                true -> {
                    getString(R.string.settingsNotesNoArchived)
                }
                else -> {
                    archiveContent
                }
            }
        }
        fragmentBinding.svArchive.visibility = View.GONE
    }

    private fun initializeListeners() {
        //Listener for note line amount spinner
        fragmentBinding.spNoteLines.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                val setTo = when(fragmentBinding.spNoteLines.selectedItemPosition){
                    0 -> -1.0
                    1 -> 0.0
                    2 -> 1.0
                    3 -> 3.0
                    4 -> 5.0
                    5 -> 10.0
                    else -> 20.0
                }
                SettingsManager.addSetting(SettingId.NOTE_LINES, setTo)
                fragmentBinding.tvCurrentNoteLines.text = resources.getStringArray(R.array.noteLines)[position]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        //Listener for note column amount spinner
        fragmentBinding.spNoteColumns.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                val value = when(fragmentBinding.spNoteColumns.selectedItemPosition){
                    0 -> "1"
                    1 -> "2"
                    else -> "3"
                }
                SettingsManager.addSetting(SettingId.NOTE_COLUMNS, value)
                fragmentBinding.tvCurrentNoteColumns.text = value
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        //Listener for note editor font size spinner
        fragmentBinding.spEditorFontSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                val value = fragmentBinding.spEditorFontSize.selectedItem as String
                //this trim is necessary to prevent possible parsing issues
                SettingsManager.addSetting(SettingId.FONT_SIZE, value.trim())
                fragmentBinding.tvCurrentNoteEditorFontSize.text = value
                fragmentBinding.tvEditorSample.textSize = value.trim().toFloat()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        fragmentBinding.swFixedNoteSize.setOnClickListener{
            SettingsManager.addSetting(SettingId.NOTES_FIXED_SIZE, fragmentBinding.swFixedNoteSize.isChecked)
            fragmentBinding.clNoteLines.visibility = when(fragmentBinding.swFixedNoteSize.isChecked){
                true -> View.GONE
                else -> View.VISIBLE
            }
        }

        fragmentBinding.swAllowSwipe.setOnClickListener {
            SettingsManager.addSetting(SettingId.NOTES_SWIPE_DELETE, fragmentBinding.swAllowSwipe.isChecked)
        }

        fragmentBinding.swRandomizeNoteColors.setOnClickListener{
            SettingsManager.addSetting(SettingId.RANDOMIZE_NOTE_COLORS, fragmentBinding.swRandomizeNoteColors.isChecked)
        }

        fragmentBinding.swShowContained.setOnClickListener{
            SettingsManager.addSetting(SettingId.NOTES_SHOW_CONTAINED, fragmentBinding.swShowContained.isChecked)
        }

        fragmentBinding.swMoveUpCurrentNote.setOnClickListener{
            SettingsManager.addSetting(SettingId.NOTES_MOVE_UP_CURRENT, fragmentBinding.swMoveUpCurrentNote.isChecked)
        }

        fragmentBinding.swSortFoldersToTop.setOnClickListener{
            SettingsManager.addSetting(SettingId.NOTES_DIRS_TO_TOP, fragmentBinding.swSortFoldersToTop.isChecked)
            if (fragmentBinding.swSortFoldersToTop.isChecked)
                MainActivity.mainNoteListDir.sortDirsToTop()
        }

        fragmentBinding.swArchive.setOnClickListener{
            SettingsManager.addSetting(SettingId.NOTES_ARCHIVE, fragmentBinding.swArchive.isChecked)
        }

        fragmentBinding.clNoteLines.setOnClickListener {
            fragmentBinding.spNoteLines.performClick()
        }

        fragmentBinding.clNoteColumns.setOnClickListener {
            fragmentBinding.spNoteColumns.performClick()
        }

        fragmentBinding.clFontSize.setOnClickListener {
            fragmentBinding.spEditorFontSize.performClick()
        }

        fragmentBinding.clShowArchive.setOnClickListener {
            if(fragmentBinding.svArchive.visibility == View.VISIBLE){
                fragmentBinding.svArchive.visibility = View.GONE
                fragmentBinding.ivArchiveExpand.rotation = 0f
            }else{
                fragmentBinding.svArchive.visibility = View.VISIBLE
                fragmentBinding.ivArchiveExpand.rotation = 180f
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    fragmentBinding.svNotesSettings.scrollToDescendant(fragmentBinding.svArchive)
                }else{
                //Todo proper scroll behavior for version < Q
                    fragmentBinding.svArchive.fullScroll(ScrollView.FOCUS_DOWN)
                }
            }

        }

        fragmentBinding.clClearArchive.setOnClickListener {
            val action: () -> Unit = {
                PreferenceManager.getDefaultSharedPreferences(myActivity).edit().putString("noteArchive", "").apply()
                fragmentBinding.tvArchive.text = getString(R.string.settingsNotesNoArchived)
                fragmentBinding.ivArchiveExpand.rotation = 0f
                fragmentBinding.svArchive.visibility = View.GONE
            }
            myActivity.dialogConfirm(getString(R.string.settingsNotesDialogDeleteArchived), action)
        }
    }
}
