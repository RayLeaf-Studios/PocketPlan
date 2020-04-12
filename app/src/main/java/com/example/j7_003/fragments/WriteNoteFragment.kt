package com.example.j7_003.fragments

import android.app.Activity
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.j7_003.MainActivity

import com.example.j7_003.R
import kotlinx.android.synthetic.main.appbar_write_note.view.*
import kotlinx.android.synthetic.main.fragment_write_note.view.*

class WriteNoteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val database = MainActivity.database
        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_write_note, container, false)
        val etNoteFrag = myView.etNoteFrag
        //todo open keyboard here
        etNoteFrag.requestFocus()
        myView.requestFocus()

        MainActivity.myActivity.supportActionBar?.customView?.btnSaveNote?.setOnClickListener(){
            val noteText = myView.etNoteFrag.text.toString()
            //todo add note here
            MainActivity.myActivity.changeToNotes()

        }
        MainActivity.myActivity.supportActionBar?.customView?.btnDiscardNote?.setOnClickListener(){
           MainActivity.myActivity.changeToNotes()
        }
        return myView
    }


}
