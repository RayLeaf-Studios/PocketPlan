package com.pocket_plan.j7_003.data.notelist


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.dialog_choose_color.view.*
import kotlinx.android.synthetic.main.dialog_discard_note_edit.view.*
import kotlinx.android.synthetic.main.fragment_note_editor.*
import kotlinx.android.synthetic.main.fragment_note_editor.view.*
import kotlinx.android.synthetic.main.title_dialog.view.*


class NoteEditorFr : Fragment() {

    private lateinit var myEtTitle: EditText
    private lateinit var myEtContent: EditText
    private var dialogOpened = false

    private lateinit var myMenu: Menu

    companion object {
        lateinit var myFragment: NoteEditorFr
        var noteColor: NoteColors = NoteColors.GREEN
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
            R.id.item_editor_color -> openColorChooser()
            R.id.item_editor_save -> {
                val noteContent = etNoteContent.text.toString()
                val noteTitle = etNoteTitle.text.toString()

                if(noteContent=="" && noteTitle==""){
                    val animationShake =
                        AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake_small)
                    etNoteContent.startAnimation(animationShake)
                    etNoteTitle.startAnimation(animationShake)
                    return true
                }
                //act as check mark to add / confirm note edit
                manageNoteConfirm()

                MainActivity.previousFragmentStack.pop()
                MainActivity.act.changeToFragment(MainActivity.previousFragmentStack.peek())
            }
        }


        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_editor, menu)
        myMenu = menu

        if (MainActivity.editNoteHolder != null) {
            myMenu.findItem(R.id.item_editor_delete)?.isVisible = true
            myMenu.findItem(R.id.item_editor_color)?.icon?.setTint(MainActivity.act.colorForAttr(MainActivity.editNoteHolder!!.color.colorCode))
        } else {
            myMenu.findItem(R.id.item_editor_color)?.icon?.setTint(MainActivity.act.colorForAttr(noteColor.colorCode))

        }
        myMenu.findItem(R.id.item_editor_delete)?.icon?.setTint(MainActivity.act.colorForAttr(R.attr.colorOnBackGround))
        myMenu.findItem(R.id.item_editor_save)?.icon?.setTint(MainActivity.act.colorForAttr(R.attr.colorOnBackGround))
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
        if (MainActivity.editNoteHolder != null && MainActivity.editNoteHolder!!.title == etNoteTitle.text.toString() &&
            MainActivity.editNoteHolder!!.content == etNoteContent.text.toString() &&
            MainActivity.editNoteHolder!!.color == noteColor
        ) {
            //no relevant note changes if the title, content and color did not get changed
            result = false
        }

        //check if anything was written when adding new note, return otherwise
        if (MainActivity.editNoteHolder == null && etNoteTitle.text.toString() == "" &&
            etNoteContent.text.toString() == ""
        ) {
            //no relevant note changes if its a new empty note
            result = false
        }
        return result
    }

    @SuppressLint("InflateParams")
    fun dialogDiscardNoteChanges(fragmentTag: FT = FT.EMPTY) {

        if (dialogOpened) {
            return
        }
        dialogOpened = true

        val myDialogView = LayoutInflater.from(MainActivity.act).inflate(
            R.layout.dialog_discard_note_edit,
            null
        )

        //AlertDialogBuilder
        val myBuilder =
            MainActivity.act.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        val customTitle = layoutInflater.inflate(R.layout.title_dialog, null)
        customTitle.tvDialogTitle.text = resources.getText(R.string.noteDiscardDialogTitle)
        myBuilder?.setCustomTitle(customTitle)

        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.show()
        myAlertDialog?.setOnCancelListener {
            MainActivity.act.setNavBarUnchecked()
            dialogOpened = false
        }

        myDialogView.btnDiscardChanges.setOnClickListener {
            if (fragmentTag != MainActivity.previousFragmentStack.pop() && fragmentTag != FT.EMPTY) {
                MainActivity.previousFragmentStack.push(fragmentTag)
            }

            dialogOpened = false
            myAlertDialog?.dismiss()
            MainActivity.act.changeToFragment(MainActivity.previousFragmentStack.peek())
        }
        myDialogView.btnSaveChanges.setOnClickListener {
            if (fragmentTag != MainActivity.previousFragmentStack.pop() && fragmentTag != FT.EMPTY) {
                MainActivity.previousFragmentStack.push(fragmentTag)
            }

            manageNoteConfirm()
            dialogOpened = false
            myAlertDialog?.dismiss()
            MainActivity.act.changeToFragment(MainActivity.previousFragmentStack.peek())
        }
    }

    private fun manageAddNote() {
        MainActivity.act.hideKeyboard()
        val noteContent = etNoteContent.text.toString()
        val noteTitle = etNoteTitle.text.toString()
        NoteFr.noteListInstance.addNote(noteTitle, noteContent, noteColor)
        val cache = MainActivity.previousFragmentStack.pop()
        if (MainActivity.previousFragmentStack.peek() == FT.HOME) {
            Toast.makeText(MainActivity.act, R.string.notificationNoteAdded, Toast.LENGTH_SHORT).show()
        }
        MainActivity.previousFragmentStack.push(cache)
    }

    private fun manageEditNote() {
        MainActivity.act.hideKeyboard()
        val noteContent = etNoteContent.text.toString()
        val noteTitle = etNoteTitle.text.toString()
        MainActivity.editNoteHolder!!.title = noteTitle
        MainActivity.editNoteHolder!!.content = noteContent
        MainActivity.editNoteHolder!!.color = noteColor
        MainActivity.editNoteHolder = null
        NoteFr.noteListInstance.save()
    }

    @SuppressLint("InflateParams")
    private fun openColorChooser() {
        //inflate the dialog with custom view
        val myDialogView = layoutInflater.inflate(R.layout.dialog_choose_color, null)

        //AlertDialogBuilder
        val myBuilder = AlertDialog.Builder(MainActivity.act).setView(myDialogView)
        val editTitle = layoutInflater.inflate(R.layout.title_dialog, null)
        editTitle.tvDialogTitle.text = getString(R.string.menuTitleColorChoose)
        myBuilder.setCustomTitle(editTitle)

        //show dialog
        val myAlertDialog = myBuilder.create()
        myAlertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog.show()

        val colorList = arrayOf(
            R.attr.colorNoteRed, R.attr.colorNoteYellow,
            R.attr.colorNoteGreen, R.attr.colorNoteBlue, R.attr.colorNotePurple
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
                    MainActivity.act.colorForAttr(colorList[i])
                )
                myAlertDialog.dismiss()
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun openDeleteNoteDialog() {
        val titleId = R.string.noteDeleteDialogText
        val action: () -> Unit = {
            NoteFr.noteListInstance.remove(MainActivity.editNoteHolder)
            MainActivity.editNoteHolder = null
            NoteFr.noteListInstance.save()
            MainActivity.act.hideKeyboard()
            MainActivity.previousFragmentStack.push(FT.EMPTY)
            MainActivity.act.changeToFragment(FT.NOTES)
        }
        MainActivity.act.dialogConfirmDelete(titleId, action)
    }
}
