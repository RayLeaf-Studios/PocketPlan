package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import kotlinx.android.synthetic.main.fragment_add_item.view.*
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

        val spNoteLines = myView.spNoteLines
        val spNoteColumns = myView.spNoteColumns

        val spAdapterNoteLines = ArrayAdapter<String>(MainActivity.myActivity, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.noteLines))
        spAdapterNoteLines.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNoteLines.adapter = spAdapterNoteLines

        val spAdapterNoteColumns = ArrayAdapter<String>(MainActivity.myActivity, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.noteColumns))
        spAdapterNoteColumns.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNoteColumns.adapter = spAdapterNoteColumns

        return myView
    }

}
