package com.pocket_plan.j7_003.data.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.databinding.FragmentSettingsMainBinding
import com.pocket_plan.j7_003.system_interaction.handler.share.BackUpActivity

class SettingsMainFr : Fragment() {

    private var _fragmentSettingsMainBinding: FragmentSettingsMainBinding? = null
    private val fragmentSettingsMainBinding get() = _fragmentSettingsMainBinding!!

    private lateinit var myActivity: MainActivity
    private lateinit var clSettingNotes: ConstraintLayout
    private lateinit var clSettingShopping: ConstraintLayout

    private lateinit var clSettingBackup: ConstraintLayout
    private lateinit var clSettingAbout: ConstraintLayout
    private lateinit var clSettingsGeneral: ConstraintLayout
    private lateinit var clSettingBirthdays: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myActivity = activity as MainActivity
        _fragmentSettingsMainBinding = FragmentSettingsMainBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        initializeComponents(fragmentSettingsMainBinding)
        initializeListeners()

        return fragmentSettingsMainBinding.root
    }

    private fun initializeComponents(fragmentSettingsMainBinding: FragmentSettingsMainBinding) {
        clSettingAbout = fragmentSettingsMainBinding.clSettingAbout
        clSettingBackup = fragmentSettingsMainBinding.clSettingBackup
        clSettingShopping = fragmentSettingsMainBinding.clSettingShopping
        clSettingNotes = fragmentSettingsMainBinding.clSettingNotes
        clSettingsGeneral = fragmentSettingsMainBinding.clSettingGeneral
        clSettingBirthdays = fragmentSettingsMainBinding.clSettingBirthdays
    }

    private fun initializeListeners() {
        clSettingNotes.setOnClickListener { myActivity.changeToFragment(FT.SETTINGS_NOTES) }
        clSettingBackup.setOnClickListener {
            val intent = Intent(myActivity, BackUpActivity::class.java)
            startActivity(intent)
            myActivity.finish()
        }
        clSettingShopping.setOnClickListener { myActivity.changeToFragment(FT.SETTINGS_SHOPPING) }
        clSettingAbout.setOnClickListener { myActivity.changeToFragment(FT.SETTINGS_ABOUT) }
        clSettingsGeneral.setOnClickListener { myActivity.changeToFragment(FT.SETTINGS_GENERAL) }
        clSettingBirthdays.setOnClickListener { myActivity.changeToFragment(FT.SETTINGS_BIRTHDAYS) }
    }

}