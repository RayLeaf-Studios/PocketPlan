package com.pocket_plan.j7_003.data.notelist

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.databinding.DialogAddNoteFolderBinding
import com.pocket_plan.j7_003.databinding.FragmentNoteBinding
import com.pocket_plan.j7_003.databinding.RowNoteBinding
import com.pocket_plan.j7_003.databinding.RowNoteFixedSizeBinding
import com.pocket_plan.j7_003.databinding.TitleDialogBinding
import java.util.Calendar
import kotlin.random.Random

/**
 * A simple [Fragment] subclass.
 */

class NoteFr : Fragment() {

    private lateinit var myMenu: Menu
    private lateinit var myRecycler: RecyclerView
    lateinit var searchView: SearchView
    lateinit var noteListDirs: NoteDirList
    lateinit var myActivity: MainActivity
    private var _frBinding: FragmentNoteBinding? = null
    private val frBinding get() = _frBinding!!

    val darkBorderStyle = SettingsManager.getSetting(SettingId.DARK_BORDER_STYLE) as Double
    val dark = SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean
    val archiveDeletedNotes = SettingsManager.getSetting(SettingId.NOTES_ARCHIVE) as Boolean
    val fixedNoteSize = SettingsManager.getSetting(SettingId.NOTES_FIXED_SIZE) as Boolean

    companion object {
        lateinit var myAdapter: NoteAdapter
        var noteLines = 0

        var deletedNote: Note? = null

        var searching = false

        lateinit var searchResults: ArrayList<Note>
        lateinit var lastQuery: String
        var editNoteHolder: Note? = null

        var displayContent: String = ""
        var displayTitle: String = ""
        var displayColor: Int = -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        myActivity = (activity as MainActivity)

        //inflating layout for NoteFragment
        val myView = inflater.inflate(R.layout.fragment_note, container, false)

        //reset deletedNote to signal no undo is possible
        deletedNote = null

        //create and set new adapter for recyclerview
        myRecycler = frBinding.recyclerViewNote
        myAdapter = NoteAdapter(myActivity, this)
        myRecycler.adapter = myAdapter

        initializeComponents()

        myAdapter.notifyDataSetChanged()
        myRecycler.scrollToPosition(0)
        return myView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_notes, menu)
        myMenu = menu
        searchResults = arrayListOf()

        //color tint for undo icon
        myMenu.getItem(0).icon?.setTint(myActivity.colorForAttr(R.attr.colorOnBackGround))

        searchView = menu.findItem(R.id.item_notes_search).actionView as SearchView
        val textListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                val imm = myActivity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                //Find the currently focused view, so we can grab the correct window token from it.
                var view: View? = myActivity.currentFocus
                //If no view currently has focus, create a new one, just so we can grab a window token from it
                if (view == null) {
                    view = View(myActivity)
                }
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (searching) {
                    search(newText.toString())
                }
                return true
            }
        }

        searchView.setOnQueryTextListener(textListener)

        //close listener to restore fragment to normal after search is finished
        val onCloseListener = SearchView.OnCloseListener {
            myActivity.toolBar.title = getString(R.string.menuTitleNotes)
            myActivity.myBtnAdd.visibility = View.VISIBLE
            searchView.onActionViewCollapsed()
            myActivity.setToolbarTitle(noteListDirs.getCurrentPathName(getString(R.string.menuTitleNotes)))
            searching = false
            setMenuAccessibility(true)
            updateNoteUndoIcon()
            myAdapter.notifyDataSetChanged()
            true
        }
        searchView.setOnCloseListener(onCloseListener)

//        onSearchCloseListener to refresh fragment once search is ended
        searchView.setOnSearchClickListener {
            myActivity.myBtnAdd.visibility = View.GONE
            myActivity.toolBar.title = ""
            searching = true
            setMenuAccessibility(false)
            updateNoteUndoIcon()
            searchResults.clear()
            myAdapter.notifyDataSetChanged()
        }

        updateNoteSearchIcon()
        updateNoteUndoIcon()
        myRecycler.scrollToPosition(0)
        super.onCreateOptionsMenu(menu, inflater)
        setMenuAccessibility(true)

    }

    private fun updateNoteSearchIcon() {
        myMenu.findItem(R.id.item_notes_search).isVisible = noteListDirs.rootDir.noteList.size > 0
    }

    private fun updateNoteUndoIcon() {
        myMenu.findItem(R.id.item_notes_undo).isVisible = deletedNote != null && !searching
    }

    fun setMenuAccessibility(state: Boolean) {
        val notInRootFolder = noteListDirs.folderStack.size > 1 && state
        myMenu.findItem(R.id.item_notes_add_folder).isVisible = state
        myMenu.findItem(R.id.item_notes_delete_folder).isVisible = notInRootFolder
        myMenu.findItem(R.id.item_notes_edit_folder).isVisible = notInRootFolder
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    fun search(query: String) {
        if (query == "") {
            searchResults.clear()
        } else {
            lastQuery = query
            searchResults.clear()

            //search all notes for occurrences of query text, add them to search results
            searchResults.addAll(noteListDirs.search(query))
        }
        myAdapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_notes_search -> {
                /* no-op, listeners for this view are implemented in onCreateOptionsMenu */
            }

            R.id.item_notes_edit_folder -> {
                if (noteListDirs.folderStack.size == 1) {
                    return true
                }
                dialogEditNoteFolder()
            }

            R.id.item_notes_undo -> {
                if (deletedNote!!.content != null) noteListDirs.addFullNote(deletedNote!!)
                else noteListDirs.addNoteDir(deletedNote!!)

                if (searching) {
                    search(lastQuery)
                } else {
                    myAdapter.notifyDataSetChanged()
                }

                deletedNote = null
                updateNoteUndoIcon()
                updateNoteSearchIcon()
            }

            R.id.item_notes_add_folder -> {
                dialogAddNoteFolder()
            }

            R.id.item_notes_delete_folder -> {
                val action: () -> Unit = {
                    val deletedDir = noteListDirs.deleteCurrentFolder()
                    if (deletedDir != null) {
                        deletedNote = deletedDir
                        archive(deletedDir)
                    }
                    myActivity.changeToFragment(FT.NOTES)
                }
                val folderName = noteListDirs.folderStack.peek().title
                val dialogTitle =
                    myActivity.getString(R.string.notesDialogDeleteFolder, folderName)
                myActivity.dialogConfirm(dialogTitle, action)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogEditNoteFolder() {
        val editFolder = noteListDirs.folderStack.peek() ?: return

        //inflate the dialog with custom view
        val myDialogBinding = DialogAddNoteFolderBinding.inflate(layoutInflater)

        //AlertDialogBuilder
        val myBuilder =
            myActivity.let { it1 -> AlertDialog.Builder(it1).setView(myDialogBinding.root) }
        val customTitleBinding = TitleDialogBinding.inflate(layoutInflater)
        customTitleBinding.tvDialogTitle.text = getString(R.string.notesEditFolder)
        myBuilder?.setCustomTitle(customTitleBinding.root)

        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()

        var folderColor = editFolder.color
        myDialogBinding.etAddNoteFolder.setText(editFolder.title)

        val btnList = arrayListOf<Button>(
            myDialogBinding.btnRed,
            myDialogBinding.btnYellow,
            myDialogBinding.btnGreen,
            myDialogBinding.btnBlue,
            myDialogBinding.btnPurple,
        )

        val backgroundList = arrayListOf<ConstraintLayout>(
            myDialogBinding.btnRedBg,
            myDialogBinding.btnYellowBg,
            myDialogBinding.btnGreenBg,
            myDialogBinding.btnBlueBg,
            myDialogBinding.btnPurpleBg,
        )

        val spFolderPaths = myDialogBinding.spFolderPaths
        val paths =
            noteListDirs.getSuperordinatePaths(editFolder, getString(R.string.menuTitleNotes))
        val spFolderAdapter = ArrayAdapter(
            myActivity, android.R.layout.simple_list_item_1,
            paths
        )

        val currentParentFolderIndex = noteListDirs.getParentFolderIndex(editFolder)

        spFolderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFolderPaths.adapter = spFolderAdapter
        spFolderPaths.setSelection(currentParentFolderIndex)

        //Initialize dark background colors if necessary
        if (dark && darkBorderStyle == 3.0) {
            backgroundList.forEachIndexed { index, constraintLayout ->
                constraintLayout.setBackgroundColor(
                    myActivity.colorForAttr(
                        getCorrespondingDarkNoteColor(NoteColors.values()[index].colorAttributeValue)
                    )
                )
            }
        }

        //White background for color of folder that is edited
        backgroundList.get(NoteColors.values().indexOf(editFolder.color))
            .setBackgroundColor(myActivity.colorForAttr(R.attr.colorOnBackGround))


        btnList.forEachIndexed { index, button ->
            //Initialize ONCLICK LISTENERS for SELECTING COLOR
            button.setOnClickListener {
                //reset all backgrounds to their respective color
                backgroundList.forEachIndexed { index, constraintLayout ->
                    var borderColor = NoteColors.values()[index].colorAttributeValue
                    if (dark && darkBorderStyle == 3.0) {
                        borderColor = getCorrespondingDarkNoteColor(borderColor)
                    }
                    constraintLayout.setBackgroundColor(myActivity.colorForAttr(borderColor))
                }
                //set white border around clicked button
                backgroundList[index].setBackgroundColor(myActivity.colorForAttr(R.attr.colorOnBackGround))

                folderColor = NoteColors.values()[index]
            }

            //INitialize dark button colors if necessary
            var buttonColor = NoteColors.values()[index].colorAttributeValue
            if (dark && darkBorderStyle == 3.0) {
                buttonColor = getCorrespondingDarkNoteColor(buttonColor)
            }
            button.setBackgroundColor(myActivity.colorForAttr(buttonColor))
        }

        myDialogBinding.btnAddNoteFolder.setOnClickListener {
            val newName = myDialogBinding.etAddNoteFolder.text.toString().trim()
            val addResult = noteListDirs.editFolder(newName, folderColor)
            if (!addResult) {
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                myDialogBinding.etAddNoteFolder.startAnimation(animationShake)
                return@setOnClickListener
            }
            if (spFolderPaths.selectedItemPosition != currentParentFolderIndex) {
                if (noteListDirs.moveDir(editFolder, spFolderPaths.selectedItemPosition)) {
                    myActivity.toast(getString(R.string.notesToastFolderMoved))
                }
            }
            myAdapter.notifyDataSetChanged()
            //reload title, current folder has been edited
            myActivity.setToolbarTitle(noteListDirs.getCurrentPathName(getString(R.string.menuTitleNotes)))
            myAlertDialog?.dismiss()
        }

        val cancelBtn = myDialogBinding.btnCancelNoteFolder
        cancelBtn.setOnClickListener { myAlertDialog?.dismiss() }

        myDialogBinding.etAddNoteFolder.requestFocus()

    }

    private fun dialogAddNoteFolder() {
        //inflate the dialog with custom view
        val myDialogBinding = DialogAddNoteFolderBinding.inflate(layoutInflater)

        //AlertDialogBuilder
        val myBuilder =
            myActivity.let { it1 -> AlertDialog.Builder(it1).setView(myDialogBinding.root) }
        val customTitleBinding = TitleDialogBinding.inflate(layoutInflater)
        //Set "Add folder" title
        customTitleBinding.tvDialogTitle.text = myActivity.getString(R.string.notesOptionAddFolder)
        myBuilder?.setCustomTitle(customTitleBinding.root)

        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()

        //Get references to color buttons and their backgrounds (used for border)
        val btnList = arrayListOf<Button>(
            myDialogBinding.btnRed,
            myDialogBinding.btnYellow,
            myDialogBinding.btnGreen,
            myDialogBinding.btnBlue,
            myDialogBinding.btnPurple,
        )

        val backgroundList = arrayListOf<ConstraintLayout>(
            myDialogBinding.btnRedBg,
            myDialogBinding.btnYellowBg,
            myDialogBinding.btnGreenBg,
            myDialogBinding.btnBlueBg,
            myDialogBinding.btnPurpleBg,
        )

        //Show the proper darker note colors if the "fill" theme is selected
        if (dark && darkBorderStyle == 3.0) {
            backgroundList.forEachIndexed { index, constraintLayout ->
                constraintLayout.setBackgroundColor(
                    myActivity.colorForAttr(
                        getCorrespondingDarkNoteColor(NoteColors.values()[index].colorAttributeValue)
                    )
                )
            }
        }

        //Get initial folder color, depending on setting
        var folderColor =
            when (SettingsManager.getSetting(SettingId.RANDOMIZE_NOTE_COLORS) as Boolean) {
                true -> {
                    val randColorIndex = Random.nextInt(0, 5)
                    NoteColors.values()[randColorIndex]
                }

                else -> {
                    val lastUsedColorIndex =
                        (SettingsManager.getSetting(SettingId.LAST_USED_NOTE_COLOR) as Double).toInt()
                    NoteColors.values()[lastUsedColorIndex]
                }
            }
        //Show initial folder color by changing the color of the background square of the selected button to colorOnBackground
        backgroundList[NoteColors.values()
            .indexOf(folderColor)].setBackgroundColor(myActivity.colorForAttr(R.attr.colorOnBackGround))

        //hide elements unnecessary for adding
        val spFolderPaths = myDialogBinding.spFolderPaths
        spFolderPaths.layoutParams.height = 0
        spFolderPaths.isClickable = false
        myDialogBinding.textView5.visibility = View.GONE

        //Onclick listeners for the color buttons, to visually reflect the users selection
        btnList.forEachIndexed { index, button ->
            button.setOnClickListener {
                //reset all backgrounds to their respective color
                backgroundList.forEachIndexed { index, constraintLayout ->
                    var borderColor = NoteColors.values()[index].colorAttributeValue
                    if (dark && darkBorderStyle == 3.0) {
                        borderColor = getCorrespondingDarkNoteColor(borderColor)
                    }
                    constraintLayout.setBackgroundColor(myActivity.colorForAttr(borderColor))
                }
                //set white border around clicked button
                backgroundList[index].setBackgroundColor(myActivity.colorForAttr(R.attr.colorOnBackGround))

                folderColor = NoteColors.values()[index]
            }

            var buttonColor = NoteColors.values()[index].colorAttributeValue
            if (dark && darkBorderStyle == 3.0) {
                buttonColor = getCorrespondingDarkNoteColor(buttonColor)
            }
            button.setBackgroundColor(myActivity.colorForAttr(buttonColor))
        }

        myDialogBinding.btnAddNoteFolder.setOnClickListener {
            val newName = myDialogBinding.etAddNoteFolder.text.toString().trim()
            val addResult = noteListDirs.addNoteDir(Note(newName, folderColor, NoteList()))
            if (!addResult) {
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                myDialogBinding.etAddNoteFolder.startAnimation(animationShake)
                return@setOnClickListener
            }
            //Save last used note color
            SettingsManager.addSetting(
                SettingId.LAST_USED_NOTE_COLOR,
                NoteColors.values().indexOf(folderColor).toDouble()
            )
            myAdapter.notifyDataSetChanged()
            myAlertDialog?.dismiss()
        }

        val cancelBtn = myDialogBinding.btnCancelNoteFolder
        cancelBtn.setOnClickListener { myAlertDialog?.dismiss() }

        myDialogBinding.etAddNoteFolder.requestFocus()
    }


    private fun initializeComponents() {
        val noteColumns = SettingsManager.getSetting(SettingId.NOTE_COLUMNS) as String

        val setting = SettingsManager.getSetting(SettingId.NOTE_LINES) as Double
        noteLines = setting.toInt()

        //initialize Recyclerview and Adapter
        myAdapter = NoteAdapter(myActivity, this)
        myRecycler.adapter = myAdapter

        //initialize and set layoutManager
        //IMPORTANT, this trim needs to stay in order to support settings that were wrongly set in 1.3.3 to "x       "
        val lm = StaggeredGridLayoutManager(noteColumns.trim().toInt(), 1)
        myRecycler.layoutManager = lm
        myRecycler.setHasFixedSize(true)

        val swipeDirections =
            when (SettingsManager.getSetting(SettingId.NOTES_SWIPE_DELETE) as Boolean) {
                true -> ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                else -> 0
            }

        //itemTouchHelper to drag and reorder notes
        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                0,
                swipeDirections
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: ViewHolder,
                    target: ViewHolder
                ): Boolean {
                    return true
                }

                override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                    val parsed = viewHolder as NoteAdapter.NoteViewHolder
                    deletedNote = parsed.noteObj

                    //delete note from noteList and save
                    noteListDirs.remove(parsed.noteObj)

                    //refresh search if searching currently
                    if (searching) {
                        search(lastQuery)
                    } else {
                        myAdapter.notifyItemRemoved(viewHolder.bindingAdapterPosition)
                    }

                    if (archiveDeletedNotes) archive(parsed.noteObj)

                    updateNoteSearchIcon()
                    updateNoteUndoIcon()
                }
            })

        itemTouchHelper.attachToRecyclerView(myRecycler)
    }

    fun archive(note: Note) {
        var currentArchiveContent =
            PreferenceManager.getDefaultSharedPreferences(myActivity).getString("noteArchive", "")
        val noteText = getContainedNoteTexts(note)
        //Append to archive, and shorten archive if its too big now
        currentArchiveContent = (noteText + currentArchiveContent).take(10000)
        //Save archive
        PreferenceManager.getDefaultSharedPreferences(myActivity).edit()
            .putString("noteArchive", currentArchiveContent).apply()

    }

    private fun getContainedNoteTexts(note: Note): String {
        var result = ""
        if (note.content == null) {
            // folder got deleted, add all contained notes and folders recursively to archive
            note.noteList.forEach {
                result += getContainedNoteTexts(it)
            }
        } else {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH) + 1 // Note: Month is 0-based in Calendar class
            val day = c.get(Calendar.DAY_OF_MONTH)
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            var newEntry =
                String.format("%02d.%02d.%04d %02d:%02d", day, month, year, hour, minute) + "\n"
            if (note.title.trim() != "") newEntry += note.title + "\n"
            //Add content
            if (note.content != null) newEntry += note.content + "\n\n"
            result += newEntry
        }
        return result
    }


    fun getCorrespondingDarkNoteColor(lightColor: Int): Int {
        return when (lightColor) {
            NoteColors.RED.colorAttributeValue -> R.attr.colorNoteRedDarker
            NoteColors.GREEN.colorAttributeValue -> R.attr.colorNoteGreenDarker
            NoteColors.BLUE.colorAttributeValue -> R.attr.colorNoteBlueDarker
            NoteColors.YELLOW.colorAttributeValue -> R.attr.colorNoteYellowDarker
            else -> R.attr.colorNotePurpleDarker
        }
    }

}

class NoteAdapter(mainActivity: MainActivity, noteFr: NoteFr) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private val myActivity = mainActivity

    private val showContained =
        SettingsManager.getSetting(SettingId.NOTES_SHOW_CONTAINED) as Boolean
    private val moveViewedToTop =
        SettingsManager.getSetting(SettingId.NOTES_MOVE_UP_CURRENT) as Boolean
    private val foldersToTop =
        SettingsManager.getSetting(SettingId.NOTES_DIRS_TO_TOP) as Boolean
    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean
    private val dark = SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean

    private val cr = myActivity.resources.getDimension(R.dimen.cornerRadius)

    private val myNoteFr = noteFr

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = when (myNoteFr.fixedNoteSize) {
            true -> RowNoteFixedSizeBinding.inflate(LayoutInflater.from(parent.context))
            false -> RowNoteBinding.inflate(LayoutInflater.from(parent.context))
        }

        return NoteViewHolder(binding as RowNoteBinding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {


        val currentNote = when (NoteFr.searching) {
            /**
             * NoteFr is currently in search mode, current note gets grabbed from
             * NoteFr. adjusted list
             */
            true -> NoteFr.searchResults[position]
            /**
             * NoteFr is currently in normal mode, current note gets grabbed from noteList
             */
            false -> myNoteFr.noteListDirs.getNote(position)
        }

        //attach note object to holder
        holder.noteObj = currentNote

        //set corner radius
        holder.binding.cvNoteCard.radius = when (round) {
            true -> cr
            else -> 0f
        }
        holder.binding.cvNoteBg.radius = holder.binding.cvNoteCard.radius

        val cardColor = when (dark) {
            //DARK THEME BACKGROUND COLORS
            true -> when (SettingsManager.getSetting(SettingId.DARK_BORDER_STYLE)) {
                //Darker colored background (filled with color but uses darker color)
                3.0 -> when (currentNote.color) {
                    NoteColors.RED -> R.attr.colorNoteRedDarker
                    NoteColors.GREEN -> R.attr.colorNoteGreenDarker
                    NoteColors.BLUE -> R.attr.colorNoteBlueDarker
                    NoteColors.YELLOW -> R.attr.colorNoteYellowDarker
                    NoteColors.PURPLE -> R.attr.colorNotePurpleDarker
                }

                //Dark background for 1 and 2
                else -> R.attr.colorBackgroundElevated

            }
            //LIGHT BACKGROUND, just use note color
            else -> currentNote.color.colorAttributeValue
        }

        val borderColor = when (dark) {
            //DARK THEME BACKGROUND COLORS
            true -> when (SettingsManager.getSetting(SettingId.DARK_BORDER_STYLE)) {
                //No border at all
                1.0 -> R.attr.colorBackgroundElevated
                //Colored border
                2.0 -> currentNote.color.colorAttributeValue
                //3.0 Darker colored background (filled with color but uses darker color)
                else -> when (currentNote.color) {
                    NoteColors.RED -> R.attr.colorNoteRedDarker
                    NoteColors.GREEN -> R.attr.colorNoteGreenDarker
                    NoteColors.BLUE -> R.attr.colorNoteBlueDarker
                    NoteColors.YELLOW -> R.attr.colorNoteYellowDarker
                    NoteColors.PURPLE -> R.attr.colorNotePurpleDarker
                }
            }
            //LIGHT BACKGROUND, just use note color as border
            else -> currentNote.color.colorAttributeValue
        }
        holder.binding.tvContainedNoteElements.setTextColor(myActivity.colorForAttr(cardColor))

        val textColor = when (dark) {
            //DARK THEME BACKGROUND COLORS
            true -> when (SettingsManager.getSetting(SettingId.DARK_BORDER_STYLE)) {
                //Filled color => white text
                3.0 -> R.attr.colorOnBackGround

                //Black background => colored text
                else -> currentNote.color.colorAttributeValue

            }
            //LIGHT BACKGROUND, white text
            else -> R.attr.colorBackground
        }

        holder.binding.cvNoteCard.setCardBackgroundColor(myActivity.colorForAttr(cardColor))
        holder.binding.cvNoteBg.setCardBackgroundColor(myActivity.colorForAttr(borderColor))

        holder.binding.tvNoteTitle.setTextColor(myActivity.colorForAttr(textColor))
        holder.binding.tvNoteContent.setTextColor(myActivity.colorForAttr(textColor))

        val moveToTop: () -> Unit = {
            if (moveViewedToTop) {
                val containingList = when (NoteFr.searching) {
                    true -> myNoteFr.noteListDirs.getParentDirectory(currentNote).noteList
                    else -> myNoteFr.noteListDirs.currentList()
                }
                val noteIndex = containingList.indexOf(currentNote)

                // if this is a note, and the setting says to move folders to the top,
                // adjust the insert index to insert after the last folder
                val insertIndex = when (foldersToTop && currentNote.content != null) {
                    false -> 0
                    true -> {
                        var index = 0
                        for (note in containingList) {
                            if (note.content == null) {
                                index += 1
                            }
                        }
                        index
                    }
                }
                containingList.removeAt(noteIndex)
                containingList.add(insertIndex, currentNote)
                myNoteFr.noteListDirs.save()
            }
        }

        if (currentNote.content != null) {
            //CONTENT AND LISTENERS FOR NOTE
            //EDITING TASK VIA ONCLICK LISTENER ON RECYCLER ITEMS
            holder.binding.tvContainedNoteElements.visibility = View.GONE
            holder.binding.root.setOnClickListener {

                //move current note to top if setting says so
                moveToTop()
                myNoteFr.noteListDirs.adjustStackAbove(currentNote)

                NoteFr.editNoteHolder = currentNote

                myActivity.hideKeyboard()
                myActivity.changeToFragment(FT.NOTE_EDITOR) as NoteEditorFr
            }

            //when title is empty, hide it else show it and set the proper text
            if (currentNote.title.trim() == "") {
                holder.binding.tvNoteTitle.visibility = View.GONE
            } else {
                holder.binding.tvNoteTitle.visibility = View.VISIBLE
                holder.binding.tvNoteTitle.text = currentNote.title
            }

            holder.binding.tvNoteContent.text = currentNote.content

            //decide how many lines per note are shown, depending on the setting noteLines (and only if note sizes are not fixed by setting)
            if (!myNoteFr.fixedNoteSize) {
                if (NoteFr.noteLines == -1) {
                    holder.binding.tvNoteContent.maxLines = Int.MAX_VALUE
                } else {
                    holder.binding.tvNoteContent.maxLines = NoteFr.noteLines
                    holder.binding.tvNoteContent.ellipsize = TextUtils.TruncateAt.END
                }

                if (NoteFr.noteLines == 0) {
                    holder.binding.tvNoteContent.maxLines = 1
                    val displayedContent = when (currentNote.content == "") {
                        true -> ""
                        false -> "..."
                    }
                    holder.binding.tvNoteContent.text = displayedContent
                }
            } else {
                // show 3 lines of text in the fixed size setting, if there is no title
                if (currentNote.title.trim() == "") {
                    holder.binding.tvNoteContent.maxLines = 3
                }
            }

            holder.binding.iconFolder.visibility = View.GONE
        } else {
            //CONTENT AND LISTENERS FOR FOLDER
            holder.binding.tvNoteTitle.text = currentNote.title
            holder.binding.tvNoteContent.text = ""
            holder.binding.tvNoteTitle.visibility = View.VISIBLE
            holder.binding.iconFolder.visibility = View.VISIBLE

            holder.binding.tvContainedNoteElements.visibility = when (showContained) {
                true -> {
                    holder.binding.tvContainedNoteElements.text =
                        holder.noteObj.noteList.size.toString()
                    View.VISIBLE
                }

                else -> View.GONE
            }

            val iconColor = when (dark) {
                true -> when (SettingsManager.getSetting(SettingId.DARK_BORDER_STYLE)) {
                    //White icon for filled colors
                    3.0 -> R.attr.colorOnBackGround

                    //colored
                    else -> currentNote.color.colorAttributeValue
                }

                //white icon for light theme
                else -> R.attr.colorBackground
            }
            holder.binding.iconFolder.setColorFilter(myActivity.colorForAttr(iconColor))

            holder.binding.root.setOnClickListener {
                //move current folder to top if setting says so
                moveToTop()

                if (NoteFr.searching) {
                    myNoteFr.noteListDirs.adjustStackAbove(currentNote)
                } else {
                    myNoteFr.noteListDirs.openFolder(currentNote)
                }
                myActivity.hideKeyboard()
                myActivity.changeToFragment(FT.NOTES)

            }
        }


    }

    override fun getItemCount(): Int {
        return when (NoteFr.searching) {
            true -> NoteFr.searchResults.size
            false -> {
                val result = myNoteFr.noteListDirs.getNoteObjCount()
                return result
            }
        }
    }

    //one instance of this class will contain one instance of row_task and meta data like position
    //also holds references to views inside the layout
    class NoteViewHolder(view: RowNoteBinding) : ViewHolder(view.root) {
        lateinit var noteObj: Note
        val binding = view
    }
}
