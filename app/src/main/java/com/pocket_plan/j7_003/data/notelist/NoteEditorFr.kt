package com.pocket_plan.j7_003.data.notelist


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.PreferenceIDs
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.dialog_add_note_folder.view.*
import kotlinx.android.synthetic.main.dialog_choose_color.view.btnBlue
import kotlinx.android.synthetic.main.dialog_choose_color.view.btnGreen
import kotlinx.android.synthetic.main.dialog_choose_color.view.btnPurple
import kotlinx.android.synthetic.main.dialog_choose_color.view.btnRed
import kotlinx.android.synthetic.main.dialog_choose_color.view.btnYellow
import kotlinx.android.synthetic.main.dialog_discard_note_edit.view.*
import kotlinx.android.synthetic.main.fragment_note_editor.*
import kotlinx.android.synthetic.main.fragment_note_editor.view.*
import kotlinx.android.synthetic.main.title_dialog.view.*
import kotlin.random.Random


class NoteEditorFr : Fragment() {

    private lateinit var myActivity: MainActivity
    private lateinit var myNoteFr: NoteFr
    private lateinit var myEtTitle: EditText
    private lateinit var myEtContent: EditText
    private var dialogOpened = false

    private val archiveDeletedNotes = SettingsManager.getSetting(SettingId.NOTES_ARCHIVE) as Boolean

    private lateinit var myMenu: Menu

    private val colorList = arrayOf(
        R.attr.colorNoteRed, R.attr.colorNoteYellow,
        R.attr.colorNoteGreen, R.attr.colorNoteBlue, R.attr.colorNotePurple
    )

    companion object {
        var noteColor: NoteColors = NoteColors.GREEN
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myActivity = activity as MainActivity
        myNoteFr = myActivity.getFragment(FT.NOTES) as NoteFr


        val myView = inflater.inflate(R.layout.fragment_note_editor, container, false)
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


        myEtTitle = myView.etNoteTitle
        myEtContent = myView.etNoteContent

        val fontSize = SettingsManager.getSetting(SettingId.FONT_SIZE).toString().trim().toFloat()

        myEtTitle.textSize = fontSize + 4
        myEtContent.textSize = fontSize

        /**
         * Prepares WriteNoteFragment, fills in necessary text and adjusts colorEdit button when = noteFr
         * called from an editing context
         */

        if (NoteFr.editNoteHolder != null) {
            if(NoteFr.displayContent != "" || NoteFr.displayTitle != ""){
                myEtContent.setText(NoteFr.displayContent)
                myEtTitle.setText(NoteFr.displayTitle)
                NoteFr.displayTitle = ""
                NoteFr.displayContent = ""
            }else{
                myEtTitle.setText(NoteFr.editNoteHolder!!.title)
                myEtContent.setText(NoteFr.editNoteHolder!!.content)
            }
            myActivity.getPreferences(Context.MODE_PRIVATE).edit().putString(PreferenceIDs.EDIT_NOTE_CONTENT.id, NoteFr.editNoteHolder!!.content!!.trim()).apply()
            myActivity.getPreferences(Context.MODE_PRIVATE).edit().putString(PreferenceIDs.EDIT_NOTE_TITLE.id, NoteFr.editNoteHolder!!.title.trim()).apply()
            myActivity.getPreferences(Context.MODE_PRIVATE).edit().putInt(PreferenceIDs.EDIT_NOTE_COLOR.id, NoteColors.values().indexOf(NoteFr.editNoteHolder!!.color)).apply()
            myEtTitle.clearFocus()
        } else {
            //Empty editNoteContent to signal we are adding a new note
            myView.etNoteTitle.setText("")
            myView.etNoteContent.setText("")
            myActivity.getPreferences(Context.MODE_PRIVATE).edit().putString(PreferenceIDs.EDIT_NOTE_CONTENT.id, "").apply()
            myActivity.getPreferences(Context.MODE_PRIVATE).edit().putString(PreferenceIDs.EDIT_NOTE_TITLE.id, "").apply()
            myActivity.getPreferences(Context.MODE_PRIVATE).edit().putInt(PreferenceIDs.EDIT_NOTE_COLOR.id, NoteColors.values().indexOf(
                noteColor)).apply()
            myEtContent.requestFocus()
            imm.toggleSoftInput(
                InputMethodManager.HIDE_IMPLICIT_ONLY,
                InputMethodManager.SHOW_FORCED
            )
        }

        return myView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_editor_delete -> openDeleteNoteDialog()

            R.id.item_editor_color -> dialogColorChooser()

            R.id.item_editor_move -> dialogMoveNote()

            R.id.item_editor_share -> {
                val noteContent = getEditorContent()
                val noteTitle = getEditorTitle()
                var fullNote = ""
                if(noteTitle != ""){
                    fullNote += noteTitle + "\n"
                }
                fullNote += noteContent
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, fullNote)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }

            R.id.item_editor_save -> {
                val noteContent = getEditorContent()
                val noteTitle = getEditorTitle()

                if (noteContent == "" && noteTitle.trim() == "") {
                    val animationShake =
                        AnimationUtils.loadAnimation(myActivity, R.anim.shake_small)
                    etNoteContent.startAnimation(animationShake)
                    etNoteTitle.startAnimation(animationShake)
                    return true
                }
                //act as check mark to add / confirm note edit
                manageNoteConfirm()

                MainActivity.previousFragmentStack.pop()
                myActivity.changeToFragment(MainActivity.previousFragmentStack.peek())
            }
        }


        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_editor, menu)
        myMenu = menu

        if (NoteFr.editNoteHolder != null) {
            //Show delete icon in menu bar
            myMenu.findItem(R.id.item_editor_delete)?.isVisible = true
            //Get color from note to be edited, to tint the color change icon
            var tintColor = when(NoteFr.displayColor != -1){
                true ->{
                    val color = NoteColors.values()[NoteFr.displayColor].colorAttributeValue
                    noteColor = NoteColors.values()[NoteFr.displayColor]
                    NoteFr.displayColor = -1
                    color
                }
                else -> {
                    noteColor = NoteFr.editNoteHolder!!.color
                    NoteFr.editNoteHolder!!.color.colorAttributeValue
                }
            }
            //Adjust it to the dark color, if dark theme and dark border style = 3.0 (fill)
            if(myNoteFr.dark && myNoteFr.darkBorderStyle == 3.0){
                tintColor = myNoteFr.getCorrespondingDarkNoteColor(tintColor)
            }
            //Apply tint to icon
            myMenu.findItem(R.id.item_editor_color)?.icon?.setTint(
                myActivity.colorForAttr(tintColor)
            )

        } else {
                if(SettingsManager.getSetting(SettingId.RANDOMIZE_NOTE_COLORS) as Boolean){
                    //init random note color if setting says so
                    val randColorIndex = Random.nextInt(0,5)
                    noteColor = NoteColors.values()[randColorIndex]

                    var tintColor = noteColor.colorAttributeValue
                    if(myNoteFr.dark && myNoteFr.darkBorderStyle == 3.0){
                        tintColor = myNoteFr.getCorrespondingDarkNoteColor(tintColor)
                    }

                    myMenu.findItem(R.id.item_editor_color)?.icon?.setTint(
                        myActivity.colorForAttr(tintColor)
                    )

                } else {
                    //init last used note color
                    val lastUsedColorIndex = (SettingsManager.getSetting(SettingId.LAST_USED_NOTE_COLOR) as Double).toInt()
                    noteColor = NoteColors.values()[lastUsedColorIndex]

                    var tintColor = noteColor.colorAttributeValue
                    if(myNoteFr.dark && myNoteFr.darkBorderStyle == 3.0){
                        tintColor = myNoteFr.getCorrespondingDarkNoteColor(tintColor)
                    }
                    myMenu.findItem(R.id.item_editor_color)?.icon?.setTint(
                        myActivity.colorForAttr(tintColor)
                    )

                }

        }

        myMenu.findItem(R.id.item_editor_delete)?.icon?.setTint(myActivity.colorForAttr(R.attr.colorOnBackGround))
        myMenu.findItem(R.id.item_editor_save)?.icon?.setTint(myActivity.colorForAttr(R.attr.colorOnBackGround))
        myMenu.findItem(R.id.item_editor_move)?.icon?.setTint(myActivity.colorForAttr(R.attr.colorOnBackGround))

        updateMenuAccessibility()

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun manageNoteConfirm() {
        if (NoteFr.editNoteHolder == null) {
            manageAddNote()
        } else {
            manageEditNote()
        }
    }

    fun getEditorContent(): String{
        return etNoteContent.text.toString().trim()
    }

    fun getEditorTitle(): String{
        return etNoteTitle.text.toString().trim()
    }

    fun getNoteColor(): Int {
        return NoteColors.values().indexOf(noteColor)
    }

    fun relevantNoteChanges(): Boolean {

        //check if note was edited, return otherwise
        if (NoteFr.editNoteHolder != null && NoteFr.editNoteHolder!!.title.trim() == getEditorTitle() &&
            //trim necessary here since older version allowed saving notes with trailing white spaces
            NoteFr.editNoteHolder!!.content!!.trim() == getEditorContent() &&
            NoteFr.editNoteHolder!!.color == noteColor
        ) {
            //no relevant note changes if the title, content and color did not get changed
            return false
        }

        //check if anything was written when adding new note, return otherwise
        if (NoteFr.editNoteHolder == null && getEditorTitle() == "" &&
            getEditorContent() == ""
        ) {
            //no relevant note changes if its a new empty note
            return false
        }

        //Either a new non-empty not was created, or a note was edited in a relevant way
        return true
    }

    @SuppressLint("InflateParams")
    fun dialogDiscardNoteChanges(fragmentTag: FT = FT.EMPTY) {

        if (dialogOpened) {
            return
        }
        dialogOpened = true

        val myDialogView = LayoutInflater.from(myActivity).inflate(
            R.layout.dialog_discard_note_edit,
            null
        )

        //AlertDialogBuilder
        val myBuilder =
            myActivity.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        val customTitle = layoutInflater.inflate(R.layout.title_dialog, null)
        customTitle.tvDialogTitle.text = resources.getText(R.string.noteDiscardDialogTitle)
        myBuilder?.setCustomTitle(customTitle)

        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.show()
        myAlertDialog?.setOnCancelListener {
            myActivity.setNavBarUnchecked()
            dialogOpened = false
        }

        myDialogView.btnDiscardChanges.setOnClickListener {
            if (fragmentTag != MainActivity.previousFragmentStack.pop() && fragmentTag != FT.EMPTY) {
                MainActivity.previousFragmentStack.push(fragmentTag)
            }

            dialogOpened = false
            myAlertDialog?.dismiss()
            myActivity.changeToFragment(MainActivity.previousFragmentStack.peek())
        }
        myDialogView.btnSaveChanges.setOnClickListener {
            if (fragmentTag != MainActivity.previousFragmentStack.pop() && fragmentTag != FT.EMPTY) {
                MainActivity.previousFragmentStack.push(fragmentTag)
            }

            manageNoteConfirm()
            dialogOpened = false
            myAlertDialog?.dismiss()
            myActivity.changeToFragment(MainActivity.previousFragmentStack.peek())
        }
    }

    private fun manageAddNote() {
        myActivity.hideKeyboard()
        val noteContent = getEditorContent()
        val noteTitle = getEditorTitle()
        myNoteFr.noteListDirs.addNote(Note(noteTitle, noteContent, noteColor))
        val cache = MainActivity.previousFragmentStack.pop()
        if (MainActivity.previousFragmentStack.peek() == FT.HOME) {
            Toast.makeText(myActivity, R.string.notesNotificationNoteAdded, Toast.LENGTH_SHORT).show()
        }
        MainActivity.previousFragmentStack.push(cache)
    }

    private fun manageEditNote() {
        myActivity.hideKeyboard()
        val noteContent = getEditorContent()
        val noteTitle = getEditorTitle()
        NoteFr.editNoteHolder!!.title = noteTitle
        NoteFr.editNoteHolder!!.content = noteContent
        NoteFr.editNoteHolder!!.color = noteColor
        NoteFr.editNoteHolder = null
        myNoteFr.noteListDirs.save()
    }

    private fun dialogMoveNote(){
        //inflate the dialog with custom view
        val myDialogView =
            LayoutInflater.from(myActivity).inflate(R.layout.dialog_move_note, null)

        //AlertDialogBuilder
        val myBuilder =
            myActivity.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        val customTitle = myActivity.layoutInflater.inflate(R.layout.title_dialog, null)
        customTitle.tvDialogTitle.text = myActivity.getString(R.string.notesConfirmMove)
        myBuilder?.setCustomTitle(customTitle)

        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()


        val spFolderPaths = myDialogView.spFolderPaths
        val paths = myNoteFr.noteListDirs.getSuperordinatePaths(NoteFr.editNoteHolder!!, getString(R.string.menuTitleNotes))
        val spFolderAdapter = ArrayAdapter(
            myActivity, android.R.layout.simple_list_item_1,
            paths
        )

        val currentParentFolderIndex = myNoteFr.noteListDirs.getParentFolderIndex(NoteFr.editNoteHolder!!)

        spFolderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFolderPaths.adapter = spFolderAdapter
        spFolderPaths.setSelection(currentParentFolderIndex)


        myDialogView.btnAddNoteFolder.setOnClickListener {
            val moveResult = myNoteFr.noteListDirs.moveDir(NoteFr.editNoteHolder!!, spFolderPaths.selectedItemPosition)
            NoteFr.myAdapter.notifyDataSetChanged()
            val moveMessage = when(moveResult){
                true -> getString(R.string.notesToastNoteMoved)
                else -> getString(R.string.notesCantMove)
            }
            myNoteFr.myActivity.toast(moveMessage)
            myAlertDialog?.dismiss()
        }

        val cancelBtn = myDialogView.btnCancelNoteFolder
        cancelBtn.setOnClickListener { myAlertDialog?.dismiss() }


    }

    @SuppressLint("InflateParams")
    private fun dialogColorChooser() {
        //inflate the dialog with custom view
        val myDialogView = layoutInflater.inflate(R.layout.dialog_choose_color, null)

        //AlertDialogBuilder
        val myBuilder = AlertDialog.Builder(myActivity).setView(myDialogView)
        val editTitle = layoutInflater.inflate(R.layout.title_dialog, null)
        editTitle.tvDialogTitle.text = getString(R.string.menuTitleColorChoose)
        myBuilder.setCustomTitle(editTitle)

        //show dialog
        val myAlertDialog = myBuilder.create()
        myAlertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog.show()

        val buttonList = arrayOf(
            myDialogView.btnRed, myDialogView.btnYellow,
            myDialogView.btnGreen, myDialogView.btnBlue, myDialogView.btnPurple
        )
        /**
         * Onclick-listeners for every specific color button
         */
        buttonList.forEachIndexed { i, b ->
            b.setOnClickListener {
                noteColor = NoteColors.values()[i]
                myMenu.findItem(R.id.item_editor_color)?.icon?.setTint(
                    myActivity.colorForAttr(colorList[i])
                )
                myAlertDialog.dismiss()
                //save last used note color
                SettingsManager.addSetting(SettingId.LAST_USED_NOTE_COLOR, i.toDouble())
            }
            var buttonColor = NoteColors.values()[i].colorAttributeValue
            if(myNoteFr.dark && myNoteFr.darkBorderStyle == 3.0){
                buttonColor = myNoteFr.getCorrespondingDarkNoteColor(buttonColor)
            }
            b.setBackgroundColor(myActivity.colorForAttr(buttonColor))
        }
    }

    @SuppressLint("InflateParams")
    private fun openDeleteNoteDialog() {
        val titleId = R.string.noteDeleteDialogText
        val action: () -> Unit = {
            myNoteFr.noteListDirs.remove(NoteFr.editNoteHolder!!)
            if(archiveDeletedNotes) myNoteFr.archive(NoteFr.editNoteHolder!!)
            NoteFr.editNoteHolder = null
            myNoteFr.noteListDirs.save()
            myActivity.hideKeyboard()
            MainActivity.previousFragmentStack.push(FT.EMPTY)
            myActivity.changeToFragment(FT.NOTES)
        }
        myActivity.dialogConfirm(titleId, action)
    }

    private fun updateMenuAccessibility(){
        myMenu.findItem(R.id.item_editor_delete).isVisible = NoteFr.editNoteHolder != null
        myMenu.findItem(R.id.item_editor_move).isVisible = NoteFr.editNoteHolder != null
    }
}
