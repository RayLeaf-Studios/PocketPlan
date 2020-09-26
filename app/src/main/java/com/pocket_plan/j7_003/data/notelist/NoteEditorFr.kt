package com.pocket_plan.j7_003.data.notelist


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.dialog_choose_color.view.*
import kotlinx.android.synthetic.main.dialog_delete_note.view.*
import kotlinx.android.synthetic.main.dialog_discard_note_edit.view.*
import kotlinx.android.synthetic.main.fragment_note_editor.*
import kotlinx.android.synthetic.main.fragment_note_editor.view.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*


class NoteEditorFr : Fragment() {

    private lateinit var myEtTitle: EditText
    private lateinit var myEtContent: EditText
    private var dialogOpened = false

    private lateinit var myMenu: Menu
    companion object{
        lateinit var myFragment: NoteEditorFr
        var noteColor: NoteColors = NoteColors.YELLOW
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        myFragment = this
        val myView = inflater.inflate(R.layout.fragment_note_editor, container, false)
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


        myEtTitle = myView.etNoteTitle
        myEtContent = myView.etNoteContent

        myEtTitle.textSize = SettingsManager.getSetting(SettingId.FONT_SIZE).toString().toFloat()
        myEtContent.textSize = SettingsManager.getSetting(SettingId.FONT_SIZE).toString().toFloat()

        /**
         * Prepares WriteNoteFragment, fills in necessary text and adjusts colorEdit button when
         * called from an editing context
         */

        if (MainActivity.editNoteHolder != null) {
            myEtTitle.setText(MainActivity.editNoteHolder!!.title)
            myEtContent.setText(MainActivity.editNoteHolder!!.content)
            myEtTitle.clearFocus()
        } else {
            myEtTitle.requestFocus()
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
            R.id.item_editor_color -> openColorChooser()
            R.id.item_editor_save -> {
                //act as check mark to add / confirm note edit
                manageNoteConfirm()
                MainActivity.activeFragmentTag = FT.EMPTY
                when (MainActivity.previousFragmentTag == FT.NOTES) {
                    true -> MainActivity.act.changeToFragment(FT.NOTES)
                    else -> MainActivity.act.changeToFragment(FT.HOME)
                }
            }
        }


        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_editor, menu)
        myMenu = menu

        if (MainActivity.editNoteHolder != null) {
            myMenu.findItem(R.id.item_editor_delete)?.isVisible = true
            myMenu.findItem(R.id.item_editor_color)?.icon?.setTint(MainActivity.editNoteHolder!!.color.resolved)
        } else {
            myMenu.findItem(R.id.item_editor_color)?.icon?.setTint(NoteColors.YELLOW.resolved)

        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun manageNoteConfirm() {

        if (MainActivity.editNoteHolder == null) {
            manageAddNote()
        } else {
            manageEditNote()
        }
    }

    fun relevantNoteChanges(): Boolean {

        var result = true
        //check if note was edited, return otherwise
        if (MainActivity.editNoteHolder != null && MainActivity.editNoteHolder!!.title == MainActivity.noteEditorFr.etNoteTitle.text.toString() &&
            MainActivity.editNoteHolder!!.content == MainActivity.noteEditorFr.etNoteContent.text.toString() &&
            MainActivity.editNoteHolder!!.color == NoteEditorFr.noteColor
        ) {
            //no relevant note changes if the title, content and color did not get changed
            result = false
        }

        //check if anything was written when adding new note, return otherwise
        if (MainActivity.editNoteHolder == null && MainActivity.noteEditorFr.etNoteTitle.text.toString() == "" &&
            MainActivity.noteEditorFr.etNoteContent.text.toString() == ""
        ) {
            //no relevant note changes if its a new empty note
            result = false
        }
        return result
    }

    @SuppressLint("InflateParams")
     fun dialogDiscardNoteChanges(gotoFragment: FT) {

        if (dialogOpened) {
            return
        }
        dialogOpened = true

        val myDialogView = LayoutInflater.from(MainActivity.act).inflate(
            R.layout.dialog_discard_note_edit,
            null
        )

        //AlertDialogBuilder
        val myBuilder = MainActivity.act.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        val customTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
        customTitle.tvDialogTitle.text = resources.getText(R.string.noteDiscardDialogTitle)
        myBuilder?.setCustomTitle(customTitle)

        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.show()
        myAlertDialog?.setOnCancelListener {
            MainActivity.act.setNavBarUnchecked()
            dialogOpened = false
        }

        myDialogView.btnDiscardChanges.setOnClickListener {
            MainActivity.activeFragmentTag = FT.EMPTY
            dialogOpened = false
            myAlertDialog?.dismiss()
            MainActivity.act.changeToFragment(gotoFragment)
        }
        myDialogView.btnSaveChanges.setOnClickListener {
            MainActivity.activeFragmentTag = FT.EMPTY
            manageNoteConfirm()
            MainActivity.act.changeToFragment(gotoFragment)
            dialogOpened = false
            myAlertDialog?.dismiss()
        }
    }

    private fun manageAddNote() {
        MainActivity.act.hideKeyboard()
        //todo, access this in a cleaner way
        val noteContent = MainActivity.noteEditorFr.etNoteContent.text.toString()
        val noteTitle = MainActivity.noteEditorFr.etNoteTitle.text.toString()
        NoteFr.noteListInstance.addNote(noteTitle, noteContent, noteColor)
        if (MainActivity.previousFragmentTag == FT.HOME) {
            Toast.makeText(MainActivity.act, "Note was added!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun manageEditNote() {
        MainActivity.act.hideKeyboard()
        val noteContent = MainActivity.noteEditorFr.etNoteContent.text.toString()
        val noteTitle = MainActivity.noteEditorFr.etNoteTitle.text.toString()
        MainActivity.editNoteHolder!!.title = noteTitle
        MainActivity.editNoteHolder!!.content = noteContent
        MainActivity.editNoteHolder!!.color = noteColor
        MainActivity.editNoteHolder = null
    }

    @SuppressLint("InflateParams")
    private fun openColorChooser() {
        //inflate the dialog with custom view
        val myDialogView = layoutInflater.inflate(R.layout.dialog_choose_color, null)

        //AlertDialogBuilder
        val myBuilder = AlertDialog.Builder(MainActivity.act).setView(myDialogView)
        val editTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
        editTitle.tvDialogTitle.text = getString(R.string.menuTitleColorChoose)
        myBuilder.setCustomTitle(editTitle)

        //show dialog
        val myAlertDialog = myBuilder.create()
        myAlertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog.show()

        val colorList = arrayOf(
            R.color.colorNoteRed, R.color.colorNoteYellow,
            R.color.colorNoteGreen, R.color.colorNoteBlue, R.color.colorNotePurple
        )
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
                    ContextCompat.getColor(
                        MainActivity.act,
                        colorList[i]
                    )
                )
                myAlertDialog.dismiss()
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun openDeleteNoteDialog() {
        val myDialogView = layoutInflater.inflate(R.layout.dialog_delete_note, null)

        //AlertDialogBuilder
        val myBuilder = AlertDialog.Builder(MainActivity.act).setView(myDialogView)
        val editTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
        editTitle.tvDialogTitle.text = resources.getText(R.string.noteDeleteDialogText)
        myBuilder.setCustomTitle(editTitle)
        val myAlertDialog = myBuilder.create()

        val btnCancelNew = myDialogView.btnCancelNew
        val btnDeleteNote = myDialogView.btnDelete
        val mySeekBar = myDialogView.sbDeleteNote

        var allowDelete = false

        mySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (progress == 100) {
                    allowDelete = true
                    btnDeleteNote.setBackgroundResource(R.drawable.round_corner_red)
                    btnDeleteNote.setTextColor(
                        ContextCompat.getColor(
                            MainActivity.act,
                            R.color.colorOnBackGround
                        )
                    )
                } else {
                    if (allowDelete) {
                        allowDelete = false
                        btnDeleteNote.setBackgroundResource(R.drawable.round_corner_gray)
                        btnDeleteNote.setTextColor(
                            ContextCompat.getColor(
                                MainActivity.act,
                                R.color.colorHint
                            )
                        )
                    }

                }

            }
        })

        btnDeleteNote.setOnClickListener {
            if (!allowDelete) {
                val animationShake =
                    AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake)
                mySeekBar.startAnimation(animationShake)
                return@setOnClickListener
            }

            NoteFr.noteListInstance.remove(MainActivity.editNoteHolder)
            MainActivity.editNoteHolder = null
            NoteFr.noteListInstance.save()
            MainActivity.act.hideKeyboard()
            myAlertDialog.dismiss()
            MainActivity.activeFragmentTag = FT.EMPTY
            MainActivity.act.changeToFragment(FT.NOTES)
        }

        btnCancelNew.setOnClickListener {
            myAlertDialog.dismiss()
        }

        //show dialog
        myAlertDialog.show()
    }
}
