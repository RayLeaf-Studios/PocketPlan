package com.pocket_plan.j7_003.data.settings.sub_categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.fragment_settings_appearance.*
import kotlinx.android.synthetic.main.fragment_settings_appearance.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsAppearanceFr : Fragment() {
    private lateinit var spTheme: Spinner
    private lateinit var spShapes: Spinner
    private lateinit var swSafetySlider: SwitchCompat
    private lateinit var swShakeTaskInHome: SwitchCompat
    private lateinit var clResetToDefault: ConstraintLayout

    companion object{
        lateinit var myFr: SettingsAppearanceFr
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_settings_appearance, container, false)
        myFr = this

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
        swSafetySlider = myView.swSafetySlider
        swShakeTaskInHome = myView.swShakeTaskInHome
        clResetToDefault = myView.clResetToDefault
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

    fun initializeDisplayValues() {
        val spThemePosition = when(SettingsManager.getSetting(SettingId.THEME)){
            true -> 1
            else -> 0
        }
        spTheme.setSelection(spThemePosition)

        val shapePosition = when(SettingsManager.getSetting(SettingId.SHAPES_ROUND)){
            true -> 1
            else -> 0
        }
        spShapes.setSelection(shapePosition)

        swSafetySlider.isChecked = SettingsManager.getSetting(SettingId.SAFETY_SLIDER_DIALOG) as Boolean
        swShakeTaskInHome.isChecked = SettingsManager.getSetting(SettingId.SHAKE_TASK_HOME) as Boolean
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

        swSafetySlider.setOnClickListener {
            SettingsManager.addSetting(SettingId.SAFETY_SLIDER_DIALOG, swSafetySlider.isChecked)
        }

        swShakeTaskInHome.setOnClickListener {
            SettingsManager.addSetting(SettingId.SHAKE_TASK_HOME, swShakeTaskInHome.isChecked)
        }

        clResetToDefault.setOnClickListener {
            MainActivity.act.resetSettings()
        }

    }
}
