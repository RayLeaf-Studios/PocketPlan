package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import kotlinx.android.synthetic.main.fragment_add_item.view.*
import kotlinx.android.synthetic.main.fragment_settings.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment() {
    lateinit var spNoteLines: Spinner
    lateinit var spNoteColumns: Spinner
    lateinit var spDefaultCategories: Spinner
    lateinit var swExpandOneCategory: Switch

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_settings, container, false)

        initializeComponents(myView)
        initializeDisplayValues()
        initializeListeners()

        return myView
    }

    private fun initializeComponents(myView: View){

        //initialize references to view
        spNoteLines = myView.spNoteLines
        spNoteColumns = myView.spNoteColumns
        spDefaultCategories = myView.spDefaultCategories
        swExpandOneCategory = myView.swExpandOneCategory

        /**
         * INITIALIZE ADAPTERS
         */

        //NOTES
        //Spinner for amount of noteLines to be displayed
        val spAdapterNoteLines = ArrayAdapter<String>(MainActivity.myActivity, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.noteLines))
        spAdapterNoteLines.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNoteLines.adapter = spAdapterNoteLines

        //SHOPPING-LIST
        //Spinner for amount of note columns
        val spAdapterNoteColumns = ArrayAdapter<String>(MainActivity.myActivity, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.noteColumns))
        spAdapterNoteColumns.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNoteColumns.adapter = spAdapterNoteColumns

        //Spinner for showing categories as expanded / hidden
        val spAdapterDefaultCategories = ArrayAdapter<String>(MainActivity.myActivity, android.R.layout.simple_list_item_1, arrayOf("expanded", "hidden"))
        spAdapterDefaultCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spDefaultCategories.adapter = spAdapterDefaultCategories
    }

    private fun initializeDisplayValues(){
        //TODO read the following 4 values from settings manager
        spNoteLines.setSelection(0)
        spNoteColumns.setSelection(0)
        spDefaultCategories.setSelection(0)
        swExpandOneCategory.isChecked = true
    }

    private fun initializeListeners(){
        spNoteLines.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val value = spNoteLines.selectedItem as String
                //TODO save Columns to SettingsManager as String
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        spNoteColumns.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val value = spNoteColumns.selectedItem as String
                val columns = value.toInt()
                //TODO save Columns to SettingsManager as Integer
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        spDefaultCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val expanded = when(spNoteColumns.selectedItem as String){
                    "expanded" -> true
                    "hidden" -> false
                    else -> false
                }

                //TODO save expanded to SettingsManager as Boolean
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        swExpandOneCategory.setOnClickListener{
            //TODO save swExpandOneCategory.isChecked to SettingsManager as Boolean
        }
    }


}


