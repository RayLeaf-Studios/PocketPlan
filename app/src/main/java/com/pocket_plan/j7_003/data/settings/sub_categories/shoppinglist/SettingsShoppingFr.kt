package com.pocket_plan.j7_003.data.settings.sub_categories.shoppinglist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.data.shoppinglist.ShoppingFr
import kotlinx.android.synthetic.main.fragment_settings_shopping.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsShoppingFr(val mainActivity: MainActivity) : Fragment() {
    private lateinit var clManageCustomItems: ConstraintLayout
    private lateinit var swExpandOneCategory: SwitchCompat
    private lateinit var swCollapseCheckedSublists: SwitchCompat
    private lateinit var swCloseItemDialog: SwitchCompat
    private lateinit var swMoveCheckedCategoriesDown: SwitchCompat
    private lateinit var swSuggestSimilarItems: SwitchCompat

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
        swMoveCheckedCategoriesDown = myView.swMoveCheckedToBottom
        swSuggestSimilarItems = myView.swSuggestSimilar

    }

    private fun initializeAdapters(){}

    private fun initializeDisplayValues() {

        swExpandOneCategory.isChecked =
            SettingsManager.getSetting(SettingId.EXPAND_ONE_CATEGORY) as Boolean

        swCollapseCheckedSublists.isChecked =
            SettingsManager.getSetting(SettingId.COLLAPSE_CHECKED_SUBLISTS) as Boolean

        swCloseItemDialog.isChecked =
            SettingsManager.getSetting(SettingId.CLOSE_ITEM_DIALOG) as Boolean

        swMoveCheckedCategoriesDown.isChecked =
            SettingsManager.getSetting(SettingId.MOVE_CHECKED_DOWN) as Boolean

        swSuggestSimilarItems.isChecked =
            SettingsManager.getSetting(SettingId.SUGGEST_SIMILAR_ITEMS) as Boolean
    }

    private fun initializeListeners() {
        //changing to custom item fragment via onclick listener
        clManageCustomItems.setOnClickListener {
            mainActivity.changeToFragment(FT.CUSTOM_ITEMS)
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

        //Switch to toggle setting to move categories below unchecked lists once they are fully checked
        swMoveCheckedCategoriesDown.setOnClickListener {
            SettingsManager.addSetting(
                SettingId.MOVE_CHECKED_DOWN,
                swMoveCheckedCategoriesDown.isChecked
            )
        }

        //Switch to toggle setting to suggest similar items when adding items to shopping list with unknown names
        swSuggestSimilarItems.setOnClickListener {
            SettingsManager.addSetting(
                SettingId.SUGGEST_SIMILAR_ITEMS,
                swSuggestSimilarItems.isChecked
            )
            ShoppingFr.suggestSimilar = swSuggestSimilarItems.isChecked
        }
    }
}
