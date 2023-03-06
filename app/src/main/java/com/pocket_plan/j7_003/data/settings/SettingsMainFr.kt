package com.pocket_plan.j7_003.data.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.system_interaction.handler.share.BackUpActivity
import kotlinx.android.synthetic.main.fragment_settings_main.view.*

class SettingsMainFr : Fragment() {

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
    ): View? {
        myActivity = activity as MainActivity
        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_settings_main, container, false)

        initializeComponents(myView)
        initializeListeners()

        return myView
    }

    private fun initializeComponents(myView: View) {
        clSettingAbout = myView.clSettingAbout
        clSettingBackup = myView.clSettingBackup
        clSettingShopping = myView.clSettingShopping
        clSettingNotes = myView.clSettingNotes
        clSettingsGeneral = myView.clSettingGeneral
        clSettingBirthdays = myView.clSettingBirthdays
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