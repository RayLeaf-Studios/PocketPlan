package com.pocket_plan.j7_003.data.notelist

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
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
import com.pocket_plan.j7_003.data.notelist.NoteEditorFr.Companion.noteColor
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.dialog_add_note_folder.view.*
import kotlinx.android.synthetic.main.dialog_add_shopping_list.view.*
import kotlinx.android.synthetic.main.fragment_multi_shopping.*
import kotlinx.android.synthetic.main.fragment_note.view.*
import kotlinx.android.synthetic.main.fragment_settings_backup.*
import kotlinx.android.synthetic.main.row_note.view.*
import kotlinx.android.synthetic.main.title_dialog.view.*
import java.util.*
import kotlin.properties.Delegates

/**
 * A simple [Fragment] subclass.
 */

class NoteFr : Fragment() {

    private lateinit var myMenu: Menu
    private lateinit var myRecycler: RecyclerView
    lateinit var searchView: SearchView
    lateinit var noteListDirs: NoteDirList
    lateinit var myActivity: MainActivity

    val darkBorderStyle = SettingsManager.getSetting(SettingId.DARK_BORDER_STYLE) as Double
    val dark = SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean

    var editFolderHolder: Note? = null

    companion object {
        lateinit var myAdapter: NoteAdapter
        var noteLines = 0

        var deletedNote: Note? = null

        var searching = false

        lateinit var searchResults: ArrayList<Note>
        lateinit var lastQuery: String
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

        //reference to recycler view
        myRecycler = myView.recycler_view_note

        //create and set new adapter for recyclerview
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
        myMenu.getItem(0).icon.setTint(myActivity.colorForAttr(R.attr.colorOnBackGround))

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
            myActivity.setToolbarTitle(noteListDirs.getCurrentPathName())
            searching = false
            myAdapter.notifyDataSetChanged()
            true
        }
        searchView.setOnCloseListener(onCloseListener)

//        onSearchCloseListener to refresh fragment once search is ended
        searchView.setOnSearchClickListener {
            myActivity.myBtnAdd.visibility = View.GONE
            myActivity.toolBar.title = ""
            searching = true
            searchResults.clear()
            myAdapter.notifyDataSetChanged()
        }

        updateNoteSearchIcon()
        updateNoteUndoIcon()
        myRecycler.scrollToPosition(0)
        super.onCreateOptionsMenu(menu, inflater)

    }

    private fun updateNoteSearchIcon() {
        myMenu.findItem(R.id.item_notes_search).isVisible = noteListDirs.rootDir.noteList.size > 0
    }

    private fun updateNoteUndoIcon() {
        myMenu.findItem(R.id.item_notes_undo).isVisible = deletedNote != null
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
            noteListDirs.currentList.forEach { note ->
                if (note.content!!.toLowerCase(Locale.ROOT)
                        .contains(query.toLowerCase(Locale.ROOT)) ||
                    note.title.toLowerCase(Locale.ROOT)
                        .contains(query.toLowerCase(Locale.ROOT))
                ) {
                    searchResults.add(note)
                }
            }
        }
        myAdapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_notes_search -> {
                /* no-op, listeners for this view are implemented in onCreateOptionsMenu */
            }

            R.id.item_notes_edit_folder ->{
                if(noteListDirs.folderStack.size == 1){
                    //todo hide option if in root folder
                    myActivity.toast("Can't edit root folder")
                    return true
                }
                editFolderHolder = noteListDirs.folderStack.peek()
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
                //Todo - Add dialog here

                val action : () -> Unit = {
                    val deletedDir = noteListDirs.deleteCurrentFolder()
                    if (deletedDir != null)
                        deletedNote = deletedDir

                    myActivity.changeToFragment(FT.NOTES)
                }
                val folderName = noteListDirs.folderStack.peek().title
                val dialogTitle = myActivity.getString(R.string.dialog_title_delete_folder, folderName)
                myActivity.dialogConfirm(dialogTitle, action)

            }

        }

        return super.onOptionsItemSelected(item)
    }

    private fun dialogEditNoteFolder(){
        if(editFolderHolder==null){
            return
        }

        //inflate the dialog with custom view
        val myDialogView =
            LayoutInflater.from(myActivity).inflate(R.layout.dialog_add_note_folder, null)

        //AlertDialogBuilder
        val myBuilder =
            myActivity.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        val customTitle = myActivity.layoutInflater.inflate(R.layout.title_dialog, null)
        customTitle.tvDialogTitle.text = getString(R.string.edit_folder)
        myBuilder?.setCustomTitle(customTitle)

        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()

        var folderColor = editFolderHolder!!.color
        myDialogView.etAddNoteFolder.setText(editFolderHolder!!.title)

        val btnList = arrayListOf<Button>(
            myDialogView.btnRed,
            myDialogView.btnYellow,
            myDialogView.btnGreen,
            myDialogView.btnBlue,
            myDialogView.btnPurple,
        )

        val backgroundList = arrayListOf<ConstraintLayout>(
            myDialogView.btnRedBg,
            myDialogView.btnYellowBg,
            myDialogView.btnGreenBg,
            myDialogView.btnBlueBg,
            myDialogView.btnPurpleBg,
        )

        val spFolderPaths = myDialogView.spFolderPaths
        val paths = noteListDirs.getSuperordinatePaths(editFolderHolder!!)
        noteListDirs.getDirPaths().toArray()
        val spFolderAdapter = ArrayAdapter(
            myActivity, android.R.layout.simple_list_item_1,
            paths
        )

        val currentParentFolderIndex = noteListDirs.getParentFolderIndex(editFolderHolder!!)

        spFolderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFolderPaths.adapter = spFolderAdapter
        spFolderPaths.setSelection(currentParentFolderIndex)

        //Initialize dark background colors if necessary
        if(dark && darkBorderStyle == 3.0){
            backgroundList.forEachIndexed { index, constraintLayout ->
                constraintLayout.setBackgroundColor(myActivity.colorForAttr(getCorrespondingDarkNoteColor(NoteColors.values()[index].colorAttributeValue)))
            }
        }

        //White background for color of folder that is edited
        backgroundList.get(NoteColors.values().indexOf(editFolderHolder!!.color)).setBackgroundColor(myActivity.colorForAttr(R.attr.colorOnBackGround))


        btnList.forEachIndexed { index, button ->
            //Initialize ONCLICK LISTENERS for SELECTING COLOR
            button.setOnClickListener {
                //reset all backgrounds to their respective color
                backgroundList.forEachIndexed { index, constraintLayout ->
                    var borderColor = NoteColors.values()[index].colorAttributeValue
                    if(dark && darkBorderStyle == 3.0){
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
            if(dark && darkBorderStyle == 3.0){
                buttonColor = getCorrespondingDarkNoteColor(buttonColor)
            }
            button.setBackgroundColor(myActivity.colorForAttr(buttonColor))
        }

        myDialogView.btnAddNoteFolder.setOnClickListener {
            val newName = myDialogView.etAddNoteFolder.text.toString()
            //Todo add check if name is allowed
            val addResult = noteListDirs.editFolder(newName, folderColor)
            noteListDirs.moveDir(editFolderHolder!!, spFolderPaths.selectedItemPosition)
            if (newName.trim() == "" || !addResult) {
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                myDialogView!!.etAddNoteFolder.startAnimation(animationShake)
                return@setOnClickListener
            }
            myAdapter.notifyDataSetChanged()
            myActivity.setToolbarTitle(noteListDirs.getCurrentPathName())
            myAlertDialog?.dismiss()
        }

        val cancelBtn = myDialogView.btnCancelNoteFolder
        cancelBtn.setOnClickListener { myAlertDialog?.dismiss() }

        myDialogView.etAddNoteFolder.requestFocus()

    }

    private fun dialogAddNoteFolder() {
        //inflate the dialog with custom view
        val myDialogView =
            LayoutInflater.from(myActivity).inflate(R.layout.dialog_add_note_folder, null)

        //AlertDialogBuilder
        val myBuilder =
            myActivity.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        val customTitle = myActivity.layoutInflater.inflate(R.layout.title_dialog, null)
        customTitle.tvDialogTitle.text = myActivity.getString(R.string.add_folder)
        myBuilder?.setCustomTitle(customTitle)

        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()

        //todo initialize random here
        var folderColor = NoteColors.YELLOW
        myDialogView.btnYellowBg.setBackgroundColor(myActivity.colorForAttr(R.attr.colorOnBackGround))

        val spFolderPaths = myDialogView.spFolderPaths
        noteListDirs.getDirPaths().toArray()

        spFolderPaths.layoutParams.height = 0
        spFolderPaths.isClickable = false
        myDialogView.textView5.visibility = View.GONE

        val btnList = arrayListOf<Button>(
            myDialogView.btnRed,
            myDialogView.btnYellow,
            myDialogView.btnGreen,
            myDialogView.btnBlue,
            myDialogView.btnPurple,
        )

        val backgroundList = arrayListOf<ConstraintLayout>(
            myDialogView.btnRedBg,
            myDialogView.btnYellowBg,
            myDialogView.btnGreenBg,
            myDialogView.btnBlueBg,
            myDialogView.btnPurpleBg,
        )

        if(dark && darkBorderStyle == 3.0){
            backgroundList.forEachIndexed { index, constraintLayout ->
               constraintLayout.setBackgroundColor(myActivity.colorForAttr(getCorrespondingDarkNoteColor(NoteColors.values()[index].colorAttributeValue)))
            }
        }

        btnList.forEachIndexed { index, button ->
            button.setOnClickListener {
                //reset all backgrounds to their respective color
                backgroundList.forEachIndexed { index, constraintLayout ->
                    var borderColor = NoteColors.values()[index].colorAttributeValue
                    if(dark && darkBorderStyle == 3.0){
                        borderColor = getCorrespondingDarkNoteColor(borderColor)
                    }
                    constraintLayout.setBackgroundColor(myActivity.colorForAttr(borderColor))
                }
                //set white border around clicked button
                backgroundList[index].setBackgroundColor(myActivity.colorForAttr(R.attr.colorOnBackGround))

                folderColor = NoteColors.values()[index]
            }

            var buttonColor = NoteColors.values()[index].colorAttributeValue
            if(dark && darkBorderStyle == 3.0){
                buttonColor = getCorrespondingDarkNoteColor(buttonColor)
            }
            button.setBackgroundColor(myActivity.colorForAttr(buttonColor))
        }

        myDialogView.btnAddNoteFolder.setOnClickListener {

            val newName = myDialogView.etAddNoteFolder.text.toString()
            //Todo add check if name is allowed
            val addResult = noteListDirs.addNoteDir(Note(newName, folderColor, NoteList()))
            if (newName.trim() == "" || !addResult) {
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                myDialogView!!.etAddNoteFolder.startAnimation(animationShake)
                return@setOnClickListener
            }
            myAdapter.notifyDataSetChanged()
            myAlertDialog?.dismiss()
        }

        val cancelBtn = myDialogView.btnCancelNoteFolder
        cancelBtn.setOnClickListener { myAlertDialog?.dismiss() }

        myDialogView.etAddNoteFolder.requestFocus()
    }


    private fun initializeComponents() {
        val noteColumns = SettingsManager.getSetting(SettingId.NOTE_COLUMNS) as String

        val setting = SettingsManager.getSetting(SettingId.NOTE_LINES) as Double
        noteLines = setting.toInt()

        //initialize Recyclerview and Adapter
        myAdapter = NoteAdapter(myActivity, this)
        myRecycler.adapter = myAdapter

        //initialize and set layoutManager
        val lm = StaggeredGridLayoutManager(noteColumns.toInt(), 1)
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

                    updateNoteSearchIcon()
                    updateNoteUndoIcon()
                }
            })

        itemTouchHelper.attachToRecyclerView(myRecycler)
    }

    fun getCorrespondingDarkNoteColor(lightColor: Int) : Int {
        return when(lightColor){
            NoteColors.RED.colorAttributeValue -> R.attr.colorNoteRedDarker
            NoteColors.GREEN.colorAttributeValue -> R.attr.colorNoteGreenDarker
            NoteColors.BLUE.colorAttributeValue -> R.attr.colorNoteBlueDarker
            NoteColors.YELLOW.colorAttributeValue -> R.attr.colorNoteYellowDarker
            else  -> R.attr.colorNotePurpleDarker
        }
    }

}

class NoteAdapter(mainActivity: MainActivity, noteFr: NoteFr) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private val myActivity = mainActivity
    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean
    private val cr = myActivity.resources.getDimension(R.dimen.cornerRadius)
    private val myNoteFr = noteFr
    private val dark = SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean
    var notePosition by Delegates.notNull<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_note, parent, false)
        return NoteViewHolder(itemView)
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
        holder.cvNoteCard.radius = when (round) {
            true -> cr
            else -> 0f
        }
        holder.itemView.cvNoteBg.radius = holder.cvNoteCard.radius

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

        holder.itemView.cvNoteCard.setCardBackgroundColor(myActivity.colorForAttr(cardColor))
        holder.itemView.cvNoteBg.setCardBackgroundColor(myActivity.colorForAttr(borderColor))

        holder.itemView.tvNoteTitle.setTextColor(myActivity.colorForAttr(textColor))
        holder.itemView.tvNoteContent.setTextColor(myActivity.colorForAttr(textColor))

        if (currentNote.content != null) {
            //CONTENT AND LISTENERS FOR NOTE
            //EDITING TASK VIA ONCLICK LISTENER ON RECYCLER ITEMS
            holder.itemView.setOnClickListener {
                noteColor = currentNote.color

                notePosition = myNoteFr.noteListDirs.currentList.indexOf(currentNote)
                //move current note to top
//                val noteToMove = holder.noteObj
//                val noteIndex = myNoteFr.noteListDirs.currentList.indexOf(currentNote)
//
//                myNoteFr.noteListDirs.currentList.removeAt(noteIndex)
//                myNoteFr.noteListDirs.currentList.add(0, noteToMove)

                PreferenceManager.getDefaultSharedPreferences(myActivity)
                    .edit().putBoolean("editingNote", true).apply()

                myActivity.changeToFragment(FT.NOTE_EDITOR) as NoteEditorFr
                myActivity.hideKeyboard()
            }

            //when title is empty, hide it else show it and set the proper text
            if (currentNote.title.trim() == "") {
                holder.tvNoteTitle.visibility = View.GONE
            } else {
                holder.tvNoteTitle.visibility = View.VISIBLE
                holder.tvNoteTitle.text = currentNote.title
            }

            holder.tvNoteContent.text = currentNote.content

            //decide how many lines per note are shown, depending on teh setting noteLines
            if (NoteFr.noteLines == -1) {
                holder.tvNoteContent.maxLines = Int.MAX_VALUE
            } else {
                holder.tvNoteContent.maxLines = NoteFr.noteLines
                holder.tvNoteContent.ellipsize = TextUtils.TruncateAt.END
            }

            if(NoteFr.noteLines == 0){
                holder.tvNoteContent.maxLines = 1
                val displayedContent = when(currentNote.content == ""){
                    true -> ""
                    false -> "..."
                }
                holder.tvNoteContent.text = displayedContent
            }

            holder.itemView.icon_folder.visibility = View.GONE
        } else {
            //CONTENT AND LISTENERS FOR FOLDER
            holder.tvNoteTitle.text = currentNote.title
            holder.tvNoteContent.text = ""
            holder.tvNoteTitle.visibility = View.VISIBLE
            holder.itemView.icon_folder.visibility = View.VISIBLE
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
            holder.itemView.icon_folder.setColorFilter(myActivity.colorForAttr(iconColor))

            holder.itemView.setOnClickListener {
                myNoteFr.noteListDirs.openFolder(currentNote)
//                notifyDataSetChanged()
//                myActivity.setToolbarTitle(myNoteFr.noteListDirs.getCurrentPathName())
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
    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNoteTitle: TextView = itemView.tvNoteTitle
        val tvNoteContent: TextView = itemView.tvNoteContent
        var cvNoteCard: CardView = itemView.cvNoteCard
        lateinit var noteObj: Note
    }

}
