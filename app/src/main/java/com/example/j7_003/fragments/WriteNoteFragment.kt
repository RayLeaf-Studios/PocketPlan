package com.example.j7_003.fragments

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.ContactsContract
import android.renderscript.ScriptGroup
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.j7_003.MainActivity

import com.example.j7_003.R
import com.example.j7_003.data.Database
import com.example.j7_003.data.NoteColors
import kotlinx.android.synthetic.main.appbar_write_note.view.*
import kotlinx.android.synthetic.main.dialog_choose_color.view.*
import kotlinx.android.synthetic.main.fragment_write_note.view.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*
import kotlin.random.Random

class WriteNoteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var noteColor = NoteColors.YELLOW
        val myView = inflater.inflate(R.layout.fragment_write_note, container, false)

        val etNoteTitle = myView.etNoteTitle
        val etNoteContent = myView.etNoteContent

        val btnColorChoose = MainActivity.myActivity.supportActionBar?.customView?.btnChooseColor
        btnColorChoose?.background =  ColorDrawable(ContextCompat.getColor(MainActivity.myActivity, R.color.colorNoteYellow))
        etNoteTitle.requestFocus()

        MainActivity.myActivity.supportActionBar?.customView?.btnSaveNote?.setOnClickListener(){
            val noteTitle = etNoteTitle.text.toString()
            val noteContent = etNoteContent.text.toString()
            Database.addNote(noteTitle, noteContent, noteColor)
            MainActivity.myActivity.changeToNotes()
        }

        MainActivity.myActivity.supportActionBar?.customView?.btnChooseColor?.setOnClickListener(){
            //inflate the dialog with custom view
            val myDialogView = LayoutInflater.from(MainActivity.myActivity).inflate(R.layout.dialog_choose_color, null)

            //AlertDialogBuilder^^
            val myBuilder = AlertDialog.Builder(MainActivity.myActivity).setView(myDialogView)
            val editTitle = LayoutInflater.from(MainActivity.myActivity).inflate(R.layout.title_dialog_add_task, null)
            editTitle.tvDialogTitle.text = "Choose color"
            myBuilder.setCustomTitle(editTitle)

            //show dialog
            val myAlertDialog = myBuilder.create()
            myAlertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            myAlertDialog.show()

            myDialogView.btnRed.setOnClickListener(){
                noteColor = NoteColors.RED
                myAlertDialog.dismiss()
                btnColorChoose?.background = ColorDrawable(ContextCompat.getColor(MainActivity.myActivity, R.color.colorNoteRed))
            }

            myDialogView.btnYellow.setOnClickListener(){
                noteColor = NoteColors.YELLOW
                myAlertDialog.dismiss()
                btnColorChoose?.background = ColorDrawable(ContextCompat.getColor(MainActivity.myActivity, R.color.colorNoteYellow))
            }

            myDialogView.btnGreen.setOnClickListener(){
                noteColor = NoteColors.GREEN
                myAlertDialog.dismiss()
                btnColorChoose?.background = ColorDrawable(ContextCompat.getColor(MainActivity.myActivity, R.color.colorNoteGreen))
            }

            myDialogView.btnBlue.setOnClickListener(){
                noteColor = NoteColors.BLUE
                myAlertDialog.dismiss()
                btnColorChoose?.background = ColorDrawable(ContextCompat.getColor(MainActivity.myActivity, R.color.colorNoteBlue))
            }

            myDialogView.btnPurple.setOnClickListener(){
                noteColor = NoteColors.PURPLE
                myAlertDialog.dismiss()
                btnColorChoose?.background = ColorDrawable(ContextCompat.getColor(MainActivity.myActivity, R.color.colorNotePurple))
            }
        }

        return myView
    }


}
