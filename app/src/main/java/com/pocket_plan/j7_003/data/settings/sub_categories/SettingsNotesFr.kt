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
import androidx.lifecycle.lifecycleScope
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.databinding.FragmentSettingsNotesBinding
import com.pocket_plan.j7_003.system_interaction.handler.storage.PreferencesHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import androidx.core.view.isVisible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first

/**
 * A simple [Fragment] subclass.
 */
class SettingsNotesFr(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : Fragment() {

    private val preferencesHandler: PreferencesHandler by inject()

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

        lifecycleScope.launch(ioDispatcher) {
            initializeAdapters()
            initializeDisplayValues()
            initializeListeners()
        }

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

    private suspend fun initializeDisplayValues() {
        val noteLinesStringIndex =
            when (preferencesHandler.read(PreferencesHandler.NOTE_LINES).first()) {
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
        fragmentBinding.tvCurrentNoteLines.text =
            resources.getStringArray(R.array.noteLines)[noteLinesStringIndex]

        val columnIndex = preferencesHandler.read(PreferencesHandler.NOTE_COLUMNS).first() - 1
        fragmentBinding.spNoteColumns.setSelection(columnIndex)

        val columnOptions = resources.getStringArray(R.array.noteColumns)
        fragmentBinding.tvCurrentNoteColumns.text = columnOptions[columnIndex]

        val fontSizeOptions = resources.getStringArray(R.array.fontSizes)
        fontSizeOptions.forEachIndexed { i, it ->
            fontSizeOptions[i] = it.trim()
        }
        val fontSizeOptionsStringIndex = fontSizeOptions.indexOf(
            preferencesHandler.read(PreferencesHandler.FONT_SIZE).first().toString()
        )
        fragmentBinding.spEditorFontSize.setSelection(fontSizeOptionsStringIndex)
        fragmentBinding.tvCurrentNoteEditorFontSize.text =
            fontSizeOptions[fontSizeOptionsStringIndex]
        fragmentBinding.tvEditorSample.textSize =
            fontSizeOptions[fontSizeOptionsStringIndex].toFloat()

        fragmentBinding.swAllowSwipe.isChecked =
            preferencesHandler.read(PreferencesHandler.NOTES_SWIPE_DELETE).first()
        fragmentBinding.swRandomizeNoteColors.isChecked =
            preferencesHandler.read(PreferencesHandler.RANDOMIZE_NOTE_COLORS).first()
        fragmentBinding.swShowContained.isChecked =
            preferencesHandler.read(PreferencesHandler.NOTES_SHOW_CONTAINED).first()
        fragmentBinding.swMoveUpCurrentNote.isChecked =
            preferencesHandler.read(PreferencesHandler.NOTES_MOVE_UP_CURRENT).first()
        fragmentBinding.swArchive.isChecked =
            preferencesHandler.read(PreferencesHandler.NOTES_ARCHIVE).first()
        fragmentBinding.swFixedNoteSize.isChecked =
            preferencesHandler.read(PreferencesHandler.NOTES_FIXED_SIZE).first()
        fragmentBinding.swSortFoldersToTop.isChecked =
            preferencesHandler.read(PreferencesHandler.NOTES_DIRS_TO_TOP).first()

        fragmentBinding.clNoteLines.visibility = when (fragmentBinding.swFixedNoteSize.isChecked) {
            true -> View.GONE
            false -> View.VISIBLE
        }

        val archiveContent = preferencesHandler.read(PreferencesHandler.NOTES_ARCHIVE_NAME).first()
        fragmentBinding.tvArchive.text = when (archiveContent.isBlank()) {
            true -> {
                getString(R.string.settingsNotesNoArchived)
            }

            false -> {
                archiveContent
            }
        }
        fragmentBinding.svArchive.visibility = View.GONE
    }

    private fun initializeListeners() {
        //Listener for note line amount spinner
        fragmentBinding.spNoteLines.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (initialDisplayNoteLines) {
                        initialDisplayNoteLines = false
                        return
                    }
                    val setTo = when (fragmentBinding.spNoteLines.selectedItemPosition) {
                        0 -> -1.0
                        1 -> 0.0
                        2 -> 1.0
                        3 -> 3.0
                        4 -> 5.0
                        5 -> 10.0
                        else -> 20.0
                    }
                    lifecycleScope.launch(ioDispatcher) {
                        preferencesHandler.save(PreferencesHandler.NOTE_LINES, setTo)
                    }
                    fragmentBinding.tvCurrentNoteLines.text =
                        resources.getStringArray(R.array.noteLines)[position]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

        //Listener for note column amount spinner
        fragmentBinding.spNoteColumns.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (initialDisplayNoteColumns) {
                        initialDisplayNoteColumns = false
                        return
                    }
                    val value = when (fragmentBinding.spNoteColumns.selectedItemPosition) {
                        0 -> 1
                        1 -> 2
                        else -> 3
                    }
                    lifecycleScope.launch(ioDispatcher) {
                        preferencesHandler.save(PreferencesHandler.NOTE_COLUMNS, value)
                    }
                    fragmentBinding.tvCurrentNoteColumns.text = value.toString()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

        //Listener for note editor font size spinner
        fragmentBinding.spEditorFontSize.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (initialDisplayFontSize) {
                        initialDisplayFontSize = false
                        return
                    }
                    val value = fragmentBinding.spEditorFontSize.selectedItem as String
                    lifecycleScope.launch(ioDispatcher) {
                        //this trim is necessary to prevent possible parsing issues
                        preferencesHandler.save(PreferencesHandler.FONT_SIZE, value.trim().toInt())
                    }
                    fragmentBinding.tvCurrentNoteEditorFontSize.text = value
                    fragmentBinding.tvEditorSample.textSize = value.trim().toFloat()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

        fragmentBinding.swFixedNoteSize.setOnClickListener {
            lifecycleScope.launch(ioDispatcher) {
                val isNoteSizeFixed = fragmentBinding.swFixedNoteSize.isChecked
                preferencesHandler.save(PreferencesHandler.NOTES_FIXED_SIZE, isNoteSizeFixed)
            }
            fragmentBinding.clNoteLines.visibility =
                when (fragmentBinding.swFixedNoteSize.isChecked) {
                    true -> View.GONE
                    else -> View.VISIBLE
                }
        }

        fragmentBinding.swAllowSwipe.setOnClickListener {
            lifecycleScope.launch(ioDispatcher) {
                val swipeAllowed = fragmentBinding.swAllowSwipe.isChecked
                preferencesHandler.save(PreferencesHandler.NOTES_SWIPE_DELETE, swipeAllowed)
            }
        }

        fragmentBinding.swRandomizeNoteColors.setOnClickListener {
            lifecycleScope.launch(ioDispatcher) {
                val randomizeNoteColors = fragmentBinding.swRandomizeNoteColors.isChecked
                preferencesHandler.save(
                    PreferencesHandler.RANDOMIZE_NOTE_COLORS,
                    randomizeNoteColors
                )
            }
        }

        fragmentBinding.swShowContained.setOnClickListener {
            lifecycleScope.launch(ioDispatcher) {
                val showContained = fragmentBinding.swShowContained.isChecked
                preferencesHandler.save(PreferencesHandler.NOTES_SHOW_CONTAINED, showContained)
            }
        }

        fragmentBinding.swMoveUpCurrentNote.setOnClickListener {
            lifecycleScope.launch(ioDispatcher) {
                val moveCurrentNode = fragmentBinding.swMoveUpCurrentNote.isChecked
                preferencesHandler.save(PreferencesHandler.NOTES_MOVE_UP_CURRENT, moveCurrentNode)
            }
        }

        fragmentBinding.swSortFoldersToTop.setOnClickListener {
            lifecycleScope.launch(ioDispatcher) {
                val sortFoldersToTop = fragmentBinding.swSortFoldersToTop.isChecked
                preferencesHandler.save(PreferencesHandler.NOTES_DIRS_TO_TOP, sortFoldersToTop)
            }
            if (fragmentBinding.swSortFoldersToTop.isChecked)
                MainActivity.mainNoteListDir.sortDirsToTop()
        }

        fragmentBinding.swArchive.setOnClickListener {
            lifecycleScope.launch(ioDispatcher) {
                val archive = fragmentBinding.swArchive.isChecked
                preferencesHandler.save(PreferencesHandler.NOTES_ARCHIVE, archive)
            }
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
            if (fragmentBinding.svArchive.isVisible) {
                fragmentBinding.svArchive.visibility = View.GONE
                fragmentBinding.ivArchiveExpand.rotation = 0f
            } else {
                fragmentBinding.svArchive.visibility = View.VISIBLE
                fragmentBinding.ivArchiveExpand.rotation = 180f
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    fragmentBinding.svNotesSettings.scrollToDescendant(fragmentBinding.svArchive)
                } else {
                    //Todo proper scroll behavior for version < Q
                    fragmentBinding.svArchive.fullScroll(ScrollView.FOCUS_DOWN)
                }
            }

        }

        fragmentBinding.clClearArchive.setOnClickListener {
            val action: () -> Unit = {
                lifecycleScope.launch(ioDispatcher) {
                    preferencesHandler.save(PreferencesHandler.NOTES_ARCHIVE_NAME, "")
                    fragmentBinding.tvArchive.text = getString(R.string.settingsNotesNoArchived)
                    fragmentBinding.ivArchiveExpand.rotation = 0f
                    fragmentBinding.svArchive.visibility = View.GONE
                }
            }
            myActivity.dialogConfirm(getString(R.string.settingsNotesDialogDeleteArchived), action)
        }
    }
}
