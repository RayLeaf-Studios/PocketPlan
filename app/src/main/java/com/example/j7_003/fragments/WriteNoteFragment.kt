package com.example.j7_003.fragments


import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.Database
import kotlinx.android.synthetic.main.fragment_write_note.view.*

class WriteNoteFragment : Fragment() {

    lateinit var myEtTitle: EditText
    lateinit var myEtContent: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val myView = inflater.inflate(R.layout.fragment_write_note, container, false)

        myEtTitle = myView.etNoteTitle
        myEtContent = myView.etNoteContent

        /**
         * Prepares WriteNoteFragment, fills in necessary text and adjusts colorEdit button when
         * called from an editing context
         */

        myEtTitle.requestFocus()

        if(MainActivity.editNoteHolder!=null){
            myEtTitle.setText(Database.getNote(MainActivity.editNoteHolder!!.adapterPosition).title)
            myEtContent.setText(Database.getNote(MainActivity.editNoteHolder!!.adapterPosition).note)
            myEtContent.requestFocus()
        }

        return myView
    }
}
