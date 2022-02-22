package com.pocket_plan.j7_003.data.settings.sub_categories

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
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
    private lateinit var myActivity: MainActivity
    private lateinit var spTheme: Spinner
    private lateinit var spShapes: Spinner
    private lateinit var spLanguages: Spinner

    private lateinit var swShakeTaskInHome: SwitchCompat
    private lateinit var swSystemTheme: SwitchCompat

    private lateinit var rgDarkBorderStyle: RadioGroup

    private lateinit var clTheme: ConstraintLayout
    private lateinit var clShapes: ConstraintLayout
    private lateinit var clLanguage: ConstraintLayout

    private lateinit var clResetToDefault: ConstraintLayout

    private lateinit var tvCurrentTheme: TextView
    private lateinit var tvCurrentShape: TextView
    private lateinit var tvCurrentLanguage: TextView

    private lateinit var cardView: CardView
    private lateinit var cardView2: CardView
    private lateinit var cardView3: CardView

    private var initialDisplayTheme: Boolean = true
    private var initialDisplayShapes: Boolean = true
    private var initialDisplayLanguage: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        myActivity = activity as MainActivity
        val myView = inflater.inflate(R.layout.fragment_settings_appearance, container, false)

        initializeComponents(myView)
        initializeAdapters()
        initializeDisplayValues()
        initializeListeners()
        updateComponentVisibility(myView)

        return myView
    }

    private fun updateComponentVisibility(myView: View) {
        val dark = SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean
        myView.crBorderTheme.visibility = when(dark){
            true -> View.VISIBLE
            else -> View.GONE
        }
        myView.dividerAboveCrBorder.visibility = when(dark){
            true -> View.VISIBLE
            else -> View.GONE
        }
    }


    private fun initializeComponents(myView: View) {

        //initialize references to view
        //spinners
        spTheme = myView.spTheme
        spShapes = myView.spShapes
        spLanguages = myView.spLanguages

        //switches
        swShakeTaskInHome = myView.swShakeTaskInHome
        swSystemTheme = myView.swSystemTheme

        //ConstraintLayouts
        clResetToDefault = myView.clResetToDefault

        //RadioGroups
        rgDarkBorderStyle = myView.rgDarkBorderStyle

        //Card views corresponding to designs for radio group
        cardView = myView.cardView
        cardView2 = myView.cardView2
        cardView3 = myView.cardView3

        clTheme = myView.clTheme
        clShapes = myView.clShapes
        clLanguage = myView.clLanguage

        tvCurrentTheme = myView.tvCurrentTheme
        tvCurrentShape = myView.tvCurrentShape
        tvCurrentLanguage = myView.tvCurrentLanguage
    }

    private fun initializeAdapters() {
        //Spinner for color theme
        val spAdapterTheme = ArrayAdapter(
            myActivity,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.themes)
        )
        spAdapterTheme.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTheme.adapter = spAdapterTheme

        //Spinner for shapes
        val spAdapterShapes = ArrayAdapter(
            myActivity,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.shapes)
        )
        spAdapterShapes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spShapes.adapter = spAdapterShapes

        //Spinner for languages
        val spAdapterLanguages = ArrayAdapter(
            myActivity,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.languages)
        )
        spAdapterLanguages.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spLanguages.adapter = spAdapterLanguages
    }


    private fun initializeDisplayValues() {
        val spThemePosition = when(SettingsManager.getSetting(SettingId.THEME_DARK)){
            //show "dark" setting
            true -> 0
            //show "light" setting
            else -> 1
        }
        spTheme.setSelection(spThemePosition)
        tvCurrentTheme.text = resources.getStringArray(R.array.themes)[spThemePosition]


        val spShapePosition = when(SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean){
            //show "round" setting
            true -> 1
            //show "normal" setting
            else -> 0
        }
        spShapes.setSelection(spShapePosition)
        tvCurrentShape.text = resources.getStringArray(R.array.shapes)[spShapePosition]

        val spLanguagePosition = when (SettingsManager.getSetting(SettingId.LANGUAGE)) {
            4.0 -> 4
            3.0 -> 3
            2.0 -> 2
            1.0 -> 1
            else -> 0
        }
        spLanguages.setSelection(spLanguagePosition)
        tvCurrentLanguage.text = resources.getStringArray(R.array.languages)[spLanguagePosition]


        swShakeTaskInHome.isChecked = SettingsManager.getSetting(SettingId.SHAKE_TASK_HOME) as Boolean
        swSystemTheme.isChecked = SettingsManager.getSetting(SettingId.USE_SYSTEM_THEME) as Boolean

        //initialize correct radio button to be checked to show correct dark border style
        val idToCheck = when(SettingsManager.getSetting(SettingId.DARK_BORDER_STYLE)) {
            1.0 -> R.id.rbBorderLess
                2.0 -> R.id.rbColoredBorder
            else -> R.id.rbFullColor
        }
        rgDarkBorderStyle.check(idToCheck)
    }

    private fun initializeListeners() {
        spLanguages.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(initialDisplayLanguage){
                    initialDisplayLanguage = false
                    return
                }
                val setTo = when(spLanguages.selectedItemPosition){
                    1 -> 1.0
                    2 -> 2.0
                    3 -> 3.0
                    4 -> 4.0
                    else -> 0.0
                }
                if(setTo!=SettingsManager.getSetting(SettingId.LANGUAGE)){
                    SettingsManager.addSetting(SettingId.LANGUAGE, setTo)
                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("NotificationEntry", "appearance")
                    startActivity(intent)
                    myActivity.finish()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        //Listener for theme spinner
        spTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(initialDisplayTheme){
                    initialDisplayTheme = false
                    return
                }

                //check if selected theme is dark theme (dark is position 0, light is 1)
                val selectedDarkTheme = spTheme.selectedItemPosition==0


                //check if use system theme is set and if current change does not conform to system theme
                //if yes, disable "use system theme"
                if(SettingsManager.getSetting(SettingId.USE_SYSTEM_THEME) as Boolean){
                    val systemDark = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                    //check if systemDarkState not equal to selected dark state
                    if(systemDark != selectedDarkTheme)
                        SettingsManager.addSetting(SettingId.USE_SYSTEM_THEME, false)
                        swSystemTheme.isChecked = false
                    }

                //check if selected dark state is equal to current dark state
                if(selectedDarkTheme != SettingsManager.getSetting(SettingId.THEME_DARK)){
                    SettingsManager.addSetting(SettingId.THEME_DARK, selectedDarkTheme)
                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("NotificationEntry", "appearance")
                    startActivity(intent)
                    myActivity.finish()
                }

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

                if(initialDisplayShapes){
                    initialDisplayShapes = false
                    return
                }

                SettingsManager.addSetting(SettingId.SHAPES_ROUND, spShapes.selectedItemPosition==1)
                tvCurrentShape.text = resources.getStringArray(R.array.shapes)[position]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        swShakeTaskInHome.setOnClickListener {
            SettingsManager.addSetting(SettingId.SHAKE_TASK_HOME, swShakeTaskInHome.isChecked)
        }

        swSystemTheme.setOnClickListener {
            SettingsManager.addSetting(SettingId.USE_SYSTEM_THEME, swSystemTheme.isChecked)

            //use system theme got disabled, current theme will stay activated
            if(!swSystemTheme.isChecked){
                return@setOnClickListener
            }

            val previousSettingDark = SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean

           //use system theme got enabled, check if system uses night mode
            when(resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES){
                //system uses night mode, add required setting
                true -> SettingsManager.addSetting(SettingId.THEME_DARK, true)

                //system does not use night mode, add required setting
                else -> SettingsManager.addSetting(SettingId.THEME_DARK, false)
            }

            //if theme got changed, trigger activity reload to load new theme
            if(previousSettingDark != SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean){
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("NotificationEntry", "appearance")
                startActivity(intent)
                myActivity.finish()
            }

        }

        //onclick listener to reset to default values
        clResetToDefault.setOnClickListener {
            val action: () -> Unit = {
                SettingsManager.restoreDefault()
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("NotificationEntry", "appearance")
                startActivity(intent)
                myActivity.finish()
            }
            myActivity.dialogConfirm(R.string.settingsAppearanceResetTitle, action, hint=getString(R.string.settingsAppearanceResetHint))
        }
        
        //listener for radio group to change dark border theme
        rgDarkBorderStyle.setOnCheckedChangeListener { _, id ->
            val newStyle = when(id){
                R.id.rbBorderLess -> 1.0
                R.id.rbColoredBorder -> 2.0
                else -> 3.0
            }
            SettingsManager.addSetting(SettingId.DARK_BORDER_STYLE, newStyle)
        }

        cardView.setOnClickListener { rbBorderLess.isChecked = true }
        cardView2.setOnClickListener { rbColoredBorder.isChecked = true }
        cardView3.setOnClickListener { rbFullColor.isChecked = true }

        clTheme.setOnClickListener {
            spTheme.performClick()
        }

        clShapes.setOnClickListener {
            spShapes.performClick()
        }

        clLanguage.setOnClickListener {
            spLanguages.performClick()
        }

    }
}
