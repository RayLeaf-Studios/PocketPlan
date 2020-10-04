package com.pocket_plan.j7_003.data.settings.sub_categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.fragment_settings_appearance.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsAppearanceFr : Fragment() {
    lateinit var spTheme: Spinner
    lateinit var spShapes: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_settings_appearance, container, false)

        initializeComponents(myView)
        initializeAdapters()
        initializeDisplayValues()
        initializeListeners()

        return myView
    }

    private fun initializeComponents(myView: View) {

        //initialize references to view
        spTheme = myView.spTheme
        spShapes = myView.spShapes
    }

    private fun initializeAdapters() {
        //Spinner for color theme
        val spAdapterTheme = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.themes)
        )
        spAdapterTheme.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTheme.adapter = spAdapterTheme

        //Spinner for shapes
        val spAdapterShapes = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.shapes)
        )
        spAdapterShapes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spShapes.adapter = spAdapterShapes

    }

    private fun initializeDisplayValues() {
        val selectedPosition = when(SettingsManager.getSetting(SettingId.SHAPES_ROUND)){
            true -> 1
            else -> 0
        }
        spTheme.setSelection(selectedPosition)

        val shapeOptions = resources.getStringArray(R.array.shapes)
        spShapes.setSelection(shapeOptions.indexOf(SettingsManager.getSetting(SettingId.SHAPES_ROUND)))

    }

    private fun initializeListeners() {

        //Listener for theme spinner
        spTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val value = spTheme.selectedItem as String
                SettingsManager.addSetting(SettingId.THEME, value)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        //Listener for shape spinner
        spShapes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                SettingsManager.addSetting(SettingId.SHAPES_ROUND, spShapes.selectedItemPosition==1)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

    }
}
