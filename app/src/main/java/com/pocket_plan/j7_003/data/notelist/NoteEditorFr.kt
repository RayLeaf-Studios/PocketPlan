package com.pocket_plan.j7_003.data.notelist


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.fragment_note_editor.view.*

class NoteEditorFr : Fragment() {

    private lateinit var myEtTitle: EditText
    private lateinit var myEtContent: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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
        }else{
            myEtTitle.requestFocus()
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.SHOW_FORCED)
        }

        return myView
    }
}
