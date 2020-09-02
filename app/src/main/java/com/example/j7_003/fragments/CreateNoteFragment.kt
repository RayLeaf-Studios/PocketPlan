package com.example.j7_003.fragments


import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.Database
import kotlinx.android.synthetic.main.fragment_write_note.view.*

class CreateNoteFragment : Fragment() {

    private lateinit var myEtTitle: EditText
    private lateinit var myEtContent: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val myView = inflater.inflate(R.layout.fragment_write_note, container, false)
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.SHOW_FORCED)

        myEtTitle = myView.etNoteTitle
        myEtContent = myView.etNoteContent

        /**
         * Prepares WriteNoteFragment, fills in necessary text and adjusts colorEdit button when
         * called from an editing context
         */

        myEtTitle.requestFocus()

        if(MainActivity.editNoteHolder!=null){
            myEtTitle.setText(Database.getNote(MainActivity.editNoteHolder!!.adapterPosition).title)
            myEtContent.setText(Database.getNote(MainActivity.editNoteHolder!!.adapterPosition).content)
            myEtContent.requestFocus()
            imm.hideSoftInputFromWindow(myView.windowToken, 0)
        }

        return myView
    }
}
