package com.example.j7_003.fragments


import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.Database
import com.example.j7_003.data.NoteColors
import kotlinx.android.synthetic.main.appbar_write_note.view.*
import kotlinx.android.synthetic.main.dialog_choose_color.view.*
import kotlinx.android.synthetic.main.fragment_write_note.*
import kotlinx.android.synthetic.main.fragment_write_note.view.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*

class WriteNoteFragment : Fragment() {

    lateinit var myEtTitle: EditText
    lateinit var myEtContent: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var noteColor = NoteColors.YELLOW
        val myView = inflater.inflate(R.layout.fragment_write_note, container, false)

        myEtTitle = myView.etNoteTitle
        myEtContent = myView.etNoteContent

        val btnColorChoose = MainActivity.myActivity.supportActionBar?.customView?.btnChooseColor

        myEtTitle.requestFocus()
        if(MainActivity.editNotePosition!=-1){
            myEtTitle.setText(Database.getNote(MainActivity.editNotePosition).title)
            myEtContent.setText(Database.getNote(MainActivity.editNotePosition).note)
            btnColorChoose?.background = when (Database.getNote(MainActivity.editNotePosition).color) {
                NoteColors.RED -> ColorDrawable(ContextCompat.getColor(MainActivity.myActivity, R.color.colorNoteRed))
                NoteColors.YELLOW -> ColorDrawable(ContextCompat.getColor(MainActivity.myActivity, R.color.colorNoteYellow))
                NoteColors.GREEN -> ColorDrawable(ContextCompat.getColor(MainActivity.myActivity, R.color.colorNoteGreen))
                NoteColors.BLUE -> ColorDrawable(ContextCompat.getColor(MainActivity.myActivity, R.color.colorNoteBlue))
                NoteColors.PURPLE -> ColorDrawable(ContextCompat.getColor(MainActivity.myActivity, R.color.colorNotePurple))
            }
            myEtContent.requestFocus()
        } else {
            btnColorChoose?.background =  ColorDrawable(ContextCompat.getColor(MainActivity.myActivity, R.color.colorNoteYellow))
        }

        MainActivity.myActivity.supportActionBar?.customView?.btnSaveNote?.setOnClickListener(){
            if(MainActivity.editNotePosition!=-1){
                val noteTitle = myEtTitle.text.toString()
                val noteContent = etNoteContent.text.toString()
                Database.editNote(MainActivity.editNotePosition, noteTitle, noteContent, noteColor)
                MainActivity.editNotePosition = -1
                MainActivity.myActivity.changeToNotes()
            }else{
                val noteTitle = myEtTitle.text.toString()
                val noteContent = etNoteContent.text.toString()
                Database.addNote(noteTitle, noteContent, noteColor)
                MainActivity.myActivity.changeToNotes()
            }

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
