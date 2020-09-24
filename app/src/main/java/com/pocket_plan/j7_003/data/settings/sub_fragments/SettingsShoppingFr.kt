package com.pocket_plan.j7_003.data.settings

import android.os.Bundle
import android.view.Gravity
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
import com.pocket_plan.j7_003.data.fragmenttags.FT
import kotlinx.android.synthetic.main.fragment_settings.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsShoppingFr : Fragment() {
    lateinit var clManageCustomItems: ConstraintLayout
    lateinit var swExpandOneCategory: SwitchCompat
    lateinit var swCollapseCheckedSublists: SwitchCompat
    lateinit var swCloseItemDialog: SwitchCompat

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_settings_shopping, container, false)

        initializeComponents(myView)
        initializeAdapters()
        initializeDisplayValues()
        initializeListeners()

        return myView
    }

    private fun initializeComponents(myView: View) {

        //initialize references to view
        clManageCustomItems = myView.clManageCustomItems

        swExpandOneCategory = myView.swExpandOneCategory
        swCollapseCheckedSublists = myView.swCollapseCheckedSublists
        swCloseItemDialog = myView.swCloseAddItemDialog

    }

    private fun initializeAdapters(){}

    private fun initializeDisplayValues() {

        swExpandOneCategory.isChecked =
            SettingsManager.getSetting(SettingId.EXPAND_ONE_CATEGORY) as Boolean
        swCollapseCheckedSublists.isChecked =
            SettingsManager.getSetting(SettingId.COLLAPSE_CHECKED_SUBLISTS) as Boolean

        swCloseItemDialog.isChecked =
            SettingsManager.getSetting(SettingId.CLOSE_ITEM_DIALOG) as Boolean
    }

    private fun initializeListeners() {
        //changing to custom item fragment via onclick listener
        clManageCustomItems.setOnClickListener {
            MainActivity.act.changeToFragment(FT.CUSTOM_ITEMS)
        }

        //Switch for only showing one category as expanded
        swExpandOneCategory.setOnClickListener {
            SettingsManager.addSetting(SettingId.EXPAND_ONE_CATEGORY, swExpandOneCategory.isChecked)
        }

        //Switch to collapse sublists when they are fully checked
        swCollapseCheckedSublists.setOnClickListener {
            SettingsManager.addSetting(
                SettingId.COLLAPSE_CHECKED_SUBLISTS,
                swCollapseCheckedSublists.isChecked
            )
        }

        //Switch to close item dialog after adding a single item
        swCloseItemDialog.setOnClickListener {
            SettingsManager.addSetting(
                SettingId.CLOSE_ITEM_DIALOG,
                swCloseItemDialog.isChecked
            )
        }
    }
}
