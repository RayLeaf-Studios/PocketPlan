package com.pocket_plan.j7_003.data.notelist


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.databinding.DialogChooseColorBinding
import com.pocket_plan.j7_003.databinding.DialogDiscardNoteEditBinding
import com.pocket_plan.j7_003.databinding.DialogMoveNoteBinding
import com.pocket_plan.j7_003.databinding.FragmentNoteEditorBinding
import com.pocket_plan.j7_003.databinding.TitleDialogBinding
import com.pocket_plan.j7_003.system_interaction.handler.storage.PreferencesHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.random.Random


class NoteEditorFr(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : Fragment() {

    private val preferencesHandler: PreferencesHandler by inject()

    private var _fragmentBinding: FragmentNoteEditorBinding? = null
    private val fragmentBinding get() = _fragmentBinding!!

    private lateinit var myActivity: MainActivity
    private lateinit var myNoteFr: NoteFr

    private var dialogOpened = false

    private val archiveDeletedNotes = preferencesHandler.read(PreferencesHandler.NOTES_ARCHIVE)

    private lateinit var myMenu: Menu

    private val colorList = arrayOf(
        R.attr.colorNoteRed, R.attr.colorNoteYellow,
        R.attr.colorNoteGreen, R.attr.colorNoteBlue, R.attr.colorNotePurple,
        R.attr.colorNoteOrange, R.attr.colorNoteLime, R.attr.colorNoteTurquoise,
        R.attr.colorNoteDarkBlue, R.attr.colorNoteDarkPurple
    )

    companion object {
        var noteColor: NoteColors = NoteColors.GREEN
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentBinding = FragmentNoteEditorBinding.inflate(inflater, container, false)
        myActivity = activity as MainActivity
        myNoteFr = myActivity.getFragment(FT.NOTES) as NoteFr

        lifecycleScope.launch(ioDispatcher) {
            val fontSize = preferencesHandler.read(PreferencesHandler.FONT_SIZE).first().toFloat()

            fragmentBinding.etNoteTitle.textSize = fontSize + 4
            fragmentBinding.etNoteContent.textSize = fontSize

            /**
             * Prepares WriteNoteFragment, fills in necessary text and adjusts colorEdit button when = noteFr
             * called from an editing context
             */

            if (NoteFr.editNoteHolder != null) {
                if (NoteFr.displayContent != "" || NoteFr.displayTitle != "") {
                    fragmentBinding.etNoteContent.setText(NoteFr.displayContent)
                    fragmentBinding.etNoteTitle.setText(NoteFr.displayTitle)
                    NoteFr.displayTitle = ""
                    NoteFr.displayContent = ""
                } else {
                    fragmentBinding.etNoteTitle.setText(NoteFr.editNoteHolder!!.title)
                    fragmentBinding.etNoteContent.setText(NoteFr.editNoteHolder!!.content)
                }

                preferencesHandler.save(
                    PreferencesHandler.EDIT_NOTE_CONTENT,
                    NoteFr.editNoteHolder!!.content!!.trim()
                )

                preferencesHandler.save(
                    PreferencesHandler.EDIT_NOTE_TITLE,
                    NoteFr.editNoteHolder!!.title.trim()
                )

                preferencesHandler.save(
                    PreferencesHandler.EDIT_NOTE_COLOR,
                    NoteColors.entries.indexOf(NoteFr.editNoteHolder!!.color)
                )

                fragmentBinding.etNoteTitle.clearFocus()
            } else {
                //Empty editNoteContent to signal we are adding a new note
                fragmentBinding.etNoteTitle.setText("")
                fragmentBinding.etNoteContent.setText("")

                preferencesHandler.save(PreferencesHandler.EDIT_NOTE_CONTENT, "")
                preferencesHandler.save(PreferencesHandler.EDIT_NOTE_TITLE, "")
                preferencesHandler.save(
                    PreferencesHandler.EDIT_NOTE_COLOR,
                    NoteColors.entries.indexOf(noteColor)
                )

                fragmentBinding.etNoteContent.requestFocus()
                fragmentBinding.etNoteContent.postDelayed({
                    val imm =
                        myActivity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(
                        fragmentBinding.etNoteContent,
                        InputMethodManager.SHOW_IMPLICIT
                    )
                }, 100)
            }
        }

        return fragmentBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_editor_delete -> openDeleteNoteDialog()

            R.id.item_editor_color -> dialogColorChooser()

            R.id.item_editor_move -> dialogMoveNote()

            R.id.item_editor_share -> {
                val noteContent = getEditorContent()
                val noteTitle = getEditorTitle()
                var fullNote = ""
                if (noteTitle != "") {
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
                    fragmentBinding.etNoteContent.startAnimation(animationShake)
                    fragmentBinding.etNoteTitle.startAnimation(animationShake)
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

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_editor, menu)
        myMenu = menu

        lifecycleScope.launch(ioDispatcher) {
            if (NoteFr.editNoteHolder != null) {
                //Show delete icon in menu bar
                myMenu.findItem(R.id.item_editor_delete)?.isVisible = true
                //Get color from note to be edited, to tint the color change icon
                var tintColor = when (NoteFr.displayColor != -1) {
                    true -> {
                        val color = NoteColors.entries[NoteFr.displayColor].colorAttributeValue
                        noteColor = NoteColors.entries.toTypedArray()[NoteFr.displayColor]
                        NoteFr.displayColor = -1
                        color
                    }

                    else -> {
                        noteColor = NoteFr.editNoteHolder!!.color
                        NoteFr.editNoteHolder!!.color.colorAttributeValue
                    }
                }
                //Adjust it to the dark color, if dark theme and dark border style = 3.0 (fill)
                if (myNoteFr.dark && myNoteFr.darkBorderStyle == 3.0) {
                    tintColor = myNoteFr.getCorrespondingDarkColor(tintColor)
                }
                //Apply tint to icon
                myMenu.findItem(R.id.item_editor_color)?.icon?.setTint(
                    myActivity.colorForAttr(tintColor)
                )

            } else {
                if (preferencesHandler.read(PreferencesHandler.RANDOMIZE_NOTE_COLORS).first()) {
                    //init random note color if setting says so
                    val randColorIndex = Random.nextInt(0, NoteColors.entries.size)
                    noteColor = NoteColors.entries.toTypedArray()[randColorIndex]

                    var tintColor = noteColor.colorAttributeValue
                    if (myNoteFr.dark && myNoteFr.darkBorderStyle == 3.0) {
                        tintColor = myNoteFr.getCorrespondingDarkColor(tintColor)
                    }

                    myMenu.findItem(R.id.item_editor_color)?.icon?.setTint(
                        myActivity.colorForAttr(tintColor)
                    )

                } else {
                    //init last used note color
                    val lastUsedColorIndex = preferencesHandler
                        .read(PreferencesHandler.LAST_USED_NOTE_COLOR)
                        .first()
                        .toInt()
                    noteColor = NoteColors.entries.toTypedArray()[lastUsedColorIndex]

                    var tintColor = noteColor.colorAttributeValue
                    if (myNoteFr.dark && myNoteFr.darkBorderStyle == 3.0) {
                        tintColor = myNoteFr.getCorrespondingDarkColor(tintColor)
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
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun manageNoteConfirm() {
        if (NoteFr.editNoteHolder == null) {
            manageAddNote()
        } else {
            manageEditNote()
        }
    }

    fun getEditorContent(): String {
        return fragmentBinding.etNoteContent.text.toString().trim()
    }

    fun getEditorTitle(): String {
        return fragmentBinding.etNoteTitle.text.toString().trim()
    }

    fun getNoteColor(): Int {
        return NoteColors.entries.indexOf(noteColor)
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

        val dialogDiscardNoteEdit = DialogDiscardNoteEditBinding.inflate(layoutInflater)

        //AlertDialogBuilder
        val myBuilder =
            myActivity.let { it1 -> AlertDialog.Builder(it1).setView(dialogDiscardNoteEdit.root) }
        val titleDialogBinding = TitleDialogBinding.inflate(layoutInflater)
        titleDialogBinding.tvDialogTitle.text = resources.getText(R.string.noteDiscardDialogTitle)
        myBuilder?.setCustomTitle(titleDialogBinding.root)

        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.show()
        myAlertDialog?.setOnCancelListener {
            myActivity.setNavBarUnchecked()
            dialogOpened = false
        }

        dialogDiscardNoteEdit.btnDiscardChanges.setOnClickListener {
            if (fragmentTag != MainActivity.previousFragmentStack.pop() && fragmentTag != FT.EMPTY) {
                MainActivity.previousFragmentStack.push(fragmentTag)
            }

            dialogOpened = false
            myAlertDialog?.dismiss()
            myActivity.changeToFragment(MainActivity.previousFragmentStack.peek())
        }
        dialogDiscardNoteEdit.btnSaveChanges.setOnClickListener {
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
            Toast.makeText(myActivity, R.string.notesNotificationNoteAdded, Toast.LENGTH_SHORT)
                .show()
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

    private fun dialogMoveNote() {
        //inflate the dialog with custom view
        val dialogMoveNoteBinding = DialogMoveNoteBinding.inflate(layoutInflater)

        //AlertDialogBuilder
        val myBuilder =
            myActivity.let { it1 -> AlertDialog.Builder(it1).setView(dialogMoveNoteBinding.root) }
        val titleDialogBinding = TitleDialogBinding.inflate(layoutInflater)
        titleDialogBinding.tvDialogTitle.text = myActivity.getString(R.string.notesConfirmMove)
        myBuilder?.setCustomTitle(titleDialogBinding.root)

        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.show()


        val spFolderPaths = dialogMoveNoteBinding.spFolderPaths
        val paths = myNoteFr.noteListDirs.getSuperordinatePaths(
            NoteFr.editNoteHolder!!,
            getString(R.string.menuTitleNotes)
        )
        val spFolderAdapter = ArrayAdapter(
            myActivity, android.R.layout.simple_list_item_1,
            paths
        )

        val currentParentFolderIndex =
            myNoteFr.noteListDirs.getParentFolderIndex(NoteFr.editNoteHolder!!)

        spFolderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFolderPaths.adapter = spFolderAdapter
        spFolderPaths.setSelection(currentParentFolderIndex)


        dialogMoveNoteBinding.btnAddNoteFolder.setOnClickListener {
            val moveResult = myNoteFr.noteListDirs.moveDir(
                NoteFr.editNoteHolder!!,
                spFolderPaths.selectedItemPosition
            )
            NoteFr.myAdapter.notifyDataSetChanged()
            val moveMessage = when (moveResult) {
                true -> getString(R.string.notesToastNoteMoved)
                else -> getString(R.string.notesCantMove)
            }
            myNoteFr.myActivity.toast(moveMessage)
            myAlertDialog?.dismiss()
        }

        val cancelBtn = dialogMoveNoteBinding.btnCancelNoteFolder
        cancelBtn.setOnClickListener { myAlertDialog?.dismiss() }


    }

    @SuppressLint("InflateParams")
    private fun dialogColorChooser() {
        //inflate the dialog with custom view
        val dialogChooseColorBinding = DialogChooseColorBinding.inflate(layoutInflater)

        //AlertDialogBuilder
        val myBuilder = AlertDialog.Builder(myActivity).setView(dialogChooseColorBinding.root)
        val titleDialogBinding = TitleDialogBinding.inflate(layoutInflater)
        titleDialogBinding.tvDialogTitle.text = getString(R.string.menuTitleColorChoose)
        myBuilder.setCustomTitle(titleDialogBinding.root)

        //show dialog
        val myAlertDialog = myBuilder.create()
        myAlertDialog.show()

        val buttonList = arrayOf(
            dialogChooseColorBinding.btnRed,
            dialogChooseColorBinding.btnYellow,
            dialogChooseColorBinding.btnGreen,
            dialogChooseColorBinding.btnBlue,
            dialogChooseColorBinding.btnPurple,
            dialogChooseColorBinding.btnOrange,
            dialogChooseColorBinding.btnLime,
            dialogChooseColorBinding.btnTurquoise,
            dialogChooseColorBinding.btnDarkBlue,
            dialogChooseColorBinding.btnDarkPurple
        )
        /**
         * Onclick-listeners for every specific color button
         */
        buttonList.forEachIndexed { i, b ->
            b.setOnClickListener {
                noteColor = NoteColors.entries.toTypedArray()[i]
                myMenu.findItem(R.id.item_editor_color)?.icon?.setTint(
                    myActivity.colorForAttr(colorList[i])
                )
                myAlertDialog.dismiss()

                lifecycleScope.launch(ioDispatcher) {
                    //save last used note color
                    preferencesHandler.save(PreferencesHandler.LAST_USED_NOTE_COLOR, i.toDouble())
                }
            }
            var buttonColor = NoteColors.entries[i].colorAttributeValue
            if (myNoteFr.dark && myNoteFr.darkBorderStyle == 3.0) {
                buttonColor = myNoteFr.getCorrespondingDarkColor(buttonColor)
            }
            b.setBackgroundColor(myActivity.colorForAttr(buttonColor))
        }
    }

    @SuppressLint("InflateParams")
    private fun openDeleteNoteDialog() {
        val titleId = R.string.noteDeleteDialogText
        val action: () -> Unit = {
            lifecycleScope.launch(ioDispatcher) {
                myNoteFr.noteListDirs.remove(NoteFr.editNoteHolder!!)
                if (archiveDeletedNotes.first()) myNoteFr.archive(NoteFr.editNoteHolder!!)
                NoteFr.editNoteHolder = null
                myNoteFr.noteListDirs.save()
                myActivity.hideKeyboard()
                MainActivity.previousFragmentStack.push(FT.EMPTY)
                myActivity.changeToFragment(FT.NOTES)
            }
        }
        myActivity.dialogConfirm(titleId, action)
    }

    private fun updateMenuAccessibility() {
        myMenu.findItem(R.id.item_editor_delete).isVisible = NoteFr.editNoteHolder != null
        myMenu.findItem(R.id.item_editor_move).isVisible = NoteFr.editNoteHolder != null
    }
}
