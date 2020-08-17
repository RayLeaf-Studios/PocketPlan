package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.j7_003.R
import kotlinx.android.synthetic.main.fragment_settings.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_settings, container, false)

        val viewNoteLines = myView.viewNoteLines
        val etNoteLines = myView.etNoteLines
        val swFullNoteContent = myView.swFullNoteContent

        var noteLines = true

        if(swFullNoteContent.isChecked){
            viewNoteLines.visibility = View.GONE
        }else{
            viewNoteLines.visibility = View.VISIBLE
        }

        swFullNoteContent.setOnClickListener {
            if(swFullNoteContent.isChecked){
                viewNoteLines.visibility = View.GONE
            }else{
                viewNoteLines.visibility = View.VISIBLE
            }
        }

        return myView
    }

}
