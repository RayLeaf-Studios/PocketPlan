package com.pocket_plan.j7_003.data.settings.sub_categories

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.databinding.FragmentSettingsGeneralBinding

/**
 * A simple [Fragment] subclass.
 */
class SettingsGeneralFr : Fragment() {
    private var _fragmentSettingsGeneralBinding: FragmentSettingsGeneralBinding? = null
    private val fragmentSettingsGeneralBinding: FragmentSettingsGeneralBinding get() = _fragmentSettingsGeneralBinding!!

    private lateinit var myActivity: MainActivity

    private var initialDisplayTheme: Boolean = true
    private var initialDisplayShapes: Boolean = true
    private var initialDisplayLanguage: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentSettingsGeneralBinding =
            FragmentSettingsGeneralBinding.inflate(inflater, container, false)

        myActivity = activity as MainActivity

        initializeAdapters()
        initializeDisplayValues()
        initializeListeners()
        updateComponentVisibility()

        return fragmentSettingsGeneralBinding.root
    }

    private fun updateComponentVisibility() {
        val dark = SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean
        fragmentSettingsGeneralBinding.crBorderTheme.visibility = when (dark) {
            true -> View.VISIBLE
            else -> View.GONE
        }
        fragmentSettingsGeneralBinding.dividerAboveCrBorder.visibility = when (dark) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun initializeAdapters() {
        //Spinner for color theme
        val spAdapterTheme = ArrayAdapter(
            myActivity,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.themes)
        )
        spAdapterTheme.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fragmentSettingsGeneralBinding.spTheme.adapter = spAdapterTheme

        //Spinner for shapes
        val spAdapterShapes = ArrayAdapter(
            myActivity,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.shapes)
        )
        spAdapterShapes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fragmentSettingsGeneralBinding.spShapes.adapter = spAdapterShapes

        //Spinner for languages
        val spAdapterLanguages = ArrayAdapter(
            myActivity,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.languages)
        )
        spAdapterLanguages.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fragmentSettingsGeneralBinding.spLanguages.adapter = spAdapterLanguages
    }


    private fun initializeDisplayValues() {
        val spThemePosition = when (SettingsManager.getSetting(SettingId.THEME_DARK)) {
            //show "dark" setting
            true -> 0
            //show "light" setting
            else -> 1
        }
        fragmentSettingsGeneralBinding.spTheme.setSelection(spThemePosition)
        fragmentSettingsGeneralBinding.tvCurrentTheme.text =
            resources.getStringArray(R.array.themes)[spThemePosition]


        val spShapePosition = when (SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean) {
            //show "round" setting
            true -> 1
            //show "normal" setting
            else -> 0
        }
        fragmentSettingsGeneralBinding.spShapes.setSelection(spShapePosition)
        fragmentSettingsGeneralBinding.tvCurrentShape.text =
            resources.getStringArray(R.array.shapes)[spShapePosition]

        val spLanguagePosition = when (SettingsManager.getSetting(SettingId.LANGUAGE)) {
            5.0 -> 5
            4.0 -> 4
            3.0 -> 3
            2.0 -> 2
            1.0 -> 1
            else -> 0
        }
        fragmentSettingsGeneralBinding.spLanguages.setSelection(spLanguagePosition)
        fragmentSettingsGeneralBinding.tvCurrentLanguage.text =
            resources.getStringArray(R.array.languages)[spLanguagePosition]


        fragmentSettingsGeneralBinding.swShakeTaskInHome.isChecked =
            SettingsManager.getSetting(SettingId.SHAKE_TASK_HOME) as Boolean
        fragmentSettingsGeneralBinding.swSystemTheme.isChecked =
            SettingsManager.getSetting(SettingId.USE_SYSTEM_THEME) as Boolean

        //initialize correct radio button to be checked to show correct dark border style
        val idToCheck = when (SettingsManager.getSetting(SettingId.DARK_BORDER_STYLE)) {
            1.0 -> R.id.rbBorderLess
            2.0 -> R.id.rbColoredBorder
            else -> R.id.rbFullColor
        }
        fragmentSettingsGeneralBinding.rgDarkBorderStyle.check(idToCheck)
    }

    private fun initializeListeners() {
        fragmentSettingsGeneralBinding.spLanguages.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (initialDisplayLanguage) {
                        initialDisplayLanguage = false
                        return
                    }
                    val setTo =
                        when (fragmentSettingsGeneralBinding.spLanguages.selectedItemPosition) {
                            1 -> 1.0
                            2 -> 2.0
                            3 -> 3.0
                            4 -> 4.0
                            5 -> 5.0
                            else -> 0.0
                        }
                    if (setTo != SettingsManager.getSetting(SettingId.LANGUAGE)) {
                        SettingsManager.addSetting(SettingId.LANGUAGE, setTo)
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("NotificationEntry", "general")
                        startActivity(intent)
                        myActivity.finish()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

        //Listener for theme spinner
        fragmentSettingsGeneralBinding.spTheme.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (initialDisplayTheme) {
                        initialDisplayTheme = false
                        return
                    }

                    //check if selected theme is dark theme (dark is position 0, light is 1)
                    val selectedDarkTheme =
                        fragmentSettingsGeneralBinding.spTheme.selectedItemPosition == 0


                    //check if use system theme is set and if current change does not conform to system theme
                    //if yes, disable "use system theme"
                    if (SettingsManager.getSetting(SettingId.USE_SYSTEM_THEME) as Boolean) {
                        val systemDark =
                            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                        //check if systemDarkState not equal to selected dark state
                        if (systemDark != selectedDarkTheme) {
                            SettingsManager.addSetting(SettingId.USE_SYSTEM_THEME, false)
                            fragmentSettingsGeneralBinding.swSystemTheme.isChecked = false
                        }
                    }

                    //check if selected dark state is equal to current dark state
                    if (selectedDarkTheme != SettingsManager.getSetting(SettingId.THEME_DARK)) {
                        SettingsManager.addSetting(SettingId.THEME_DARK, selectedDarkTheme)
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("NotificationEntry", "general")
                        startActivity(intent)
                        myActivity.finish()
                    }

                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

        //Listener for shape spinner
        fragmentSettingsGeneralBinding.spShapes.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (initialDisplayShapes) {
                        initialDisplayShapes = false
                        return
                    }

                    SettingsManager.addSetting(
                        SettingId.SHAPES_ROUND,
                        fragmentSettingsGeneralBinding.spShapes.selectedItemPosition == 1
                    )
                    fragmentSettingsGeneralBinding.tvCurrentShape.text =
                        resources.getStringArray(R.array.shapes)[position]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

        fragmentSettingsGeneralBinding.swShakeTaskInHome.setOnClickListener {
            SettingsManager.addSetting(
                SettingId.SHAKE_TASK_HOME,
                fragmentSettingsGeneralBinding.swShakeTaskInHome.isChecked
            )
        }

        fragmentSettingsGeneralBinding.swSystemTheme.setOnClickListener {
            SettingsManager.addSetting(
                SettingId.USE_SYSTEM_THEME,
                fragmentSettingsGeneralBinding.swSystemTheme.isChecked
            )

            //use system theme got disabled, current theme will stay activated
            if (!fragmentSettingsGeneralBinding.swSystemTheme.isChecked) {
                return@setOnClickListener
            }

            val previousSettingDark = SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean

            //use system theme got enabled, check if system uses night mode
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                //system uses night mode, add required setting
                true -> SettingsManager.addSetting(SettingId.THEME_DARK, true)

                //system does not use night mode, add required setting
                else -> SettingsManager.addSetting(SettingId.THEME_DARK, false)
            }

            //if theme got changed, trigger activity reload to load new theme
            if (previousSettingDark != SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean) {
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("NotificationEntry", "general")
                startActivity(intent)
                myActivity.finish()
            }

        }

        //onclick listener to reset to default values
        fragmentSettingsGeneralBinding.clResetToDefault.setOnClickListener {
            val action: () -> Unit = {
                SettingsManager.restoreDefault()
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("NotificationEntry", "general")
                startActivity(intent)
                myActivity.finish()
            }
            myActivity.dialogConfirm(
                R.string.settingsGeneralResetTitle,
                action,
                hint = getString(R.string.settingsGeneralResetHint)
            )
        }

        //listener for radio group to change dark border theme
        fragmentSettingsGeneralBinding.rgDarkBorderStyle.setOnCheckedChangeListener { _, id ->
            val newStyle = when (id) {
                R.id.rbBorderLess -> 1.0
                R.id.rbColoredBorder -> 2.0
                else -> 3.0
            }
            SettingsManager.addSetting(SettingId.DARK_BORDER_STYLE, newStyle)
        }

        fragmentSettingsGeneralBinding.cardView.setOnClickListener {
            fragmentSettingsGeneralBinding.rbBorderLess.isChecked = true
        }
        fragmentSettingsGeneralBinding.cardView2.setOnClickListener {
            fragmentSettingsGeneralBinding.rbColoredBorder.isChecked = true
        }
        fragmentSettingsGeneralBinding.cardView3.setOnClickListener {
            fragmentSettingsGeneralBinding.rbFullColor.isChecked = true
        }

        fragmentSettingsGeneralBinding.clTheme.setOnClickListener {
            fragmentSettingsGeneralBinding.spTheme.performClick()
        }

        fragmentSettingsGeneralBinding.clShapes.setOnClickListener {
            fragmentSettingsGeneralBinding.spShapes.performClick()
        }

        fragmentSettingsGeneralBinding.clLanguage.setOnClickListener {
            fragmentSettingsGeneralBinding.spLanguages.performClick()
        }
    }
}
