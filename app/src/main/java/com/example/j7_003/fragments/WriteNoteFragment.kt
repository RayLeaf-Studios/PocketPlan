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

        /**
         * Prepares WriteNoteFragment, fills in necessary text and adjusts colorEdit button when
         * called from an editing context
         */

        myEtTitle.requestFocus()
        if(MainActivity.holder!=null){
            noteColor = Database.getNote(MainActivity.holder!!.adapterPosition).color
            myEtTitle.setText(Database.getNote(MainActivity.holder!!.adapterPosition).title)
            myEtContent.setText(Database.getNote(MainActivity.holder!!.adapterPosition).note)
            btnColorChoose?.background = when (Database.getNote(MainActivity.holder!!.adapterPosition).color) {
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

        /**
         * OnclickListener for saveButton, saving or editing task depending on
         * MainActivity.editNotePosition
         */

        MainActivity.myActivity.supportActionBar?.customView?.btnSaveNote?.setOnClickListener(){
            if(MainActivity.holder!=null){
                val noteTitle = myEtTitle.text.toString()
                val noteContent = etNoteContent.text.toString()
                Database.editNote(MainActivity.holder!!.adapterPosition, noteTitle, noteContent, noteColor)
                MainActivity.holder = null
                MainActivity.myActivity.changeToNotes()
            }else{
                val noteTitle = myEtTitle.text.toString()
                val noteContent = etNoteContent.text.toString()
                Database.addNote(noteTitle, noteContent, noteColor)
                MainActivity.myActivity.changeToNotes()
            }

        }

        /**
         * Dialog for Note-Color-Choose Button
         */

        MainActivity.myActivity.supportActionBar?.customView?.btnChooseColor?.setOnClickListener(){
            //inflate the dialog with custom view
            val myDialogView = LayoutInflater.from(MainActivity.myActivity).inflate(R.layout.dialog_choose_color, null)

            //AlertDialogBuilder
            val myBuilder = AlertDialog.Builder(MainActivity.myActivity).setView(myDialogView)
            val editTitle = LayoutInflater.from(MainActivity.myActivity).inflate(R.layout.title_dialog_add_task, null)
            editTitle.tvDialogTitle.text = "Choose color"
            myBuilder.setCustomTitle(editTitle)

            //show dialog
            val myAlertDialog = myBuilder.create()
            myAlertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            myAlertDialog.show()

            val colorList = arrayOf(R.color.colorNoteRed, R.color.colorNoteYellow,
                R.color.colorNoteGreen, R.color.colorNoteBlue, R.color.colorNotePurple)
            val buttonList = arrayOf(myDialogView.btnRed, myDialogView.btnYellow,
                myDialogView.btnGreen, myDialogView.btnBlue, myDialogView.btnPurple)
            /**
             * Onclick-listeners for every specific color button
             */
            buttonList.forEachIndexed(){ i, b ->
                b.setOnClickListener(){
                    noteColor = NoteColors.values()[i]
                    btnColorChoose?.background = ColorDrawable(ContextCompat.getColor(MainActivity.myActivity, colorList[i]))
                    myAlertDialog.dismiss()
                }
            }
        }

        return myView
    }

}
