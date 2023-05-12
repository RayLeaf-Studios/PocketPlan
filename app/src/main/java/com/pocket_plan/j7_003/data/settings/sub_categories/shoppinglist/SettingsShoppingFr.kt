package com.pocket_plan.j7_003.data.settings.sub_categories.shoppinglist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.data.shoppinglist.ShoppingFr
import com.pocket_plan.j7_003.databinding.FragmentSettingsShoppingBinding

/**
 * A simple [Fragment] subclass.
 */
class SettingsShoppingFr : Fragment() {
    private var _fragmentBinding: FragmentSettingsShoppingBinding? = null
    private val fragmentBinding get() = _fragmentBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentBinding = FragmentSettingsShoppingBinding.inflate(inflater, container, false)

        initializeDisplayValues()
        initializeListeners()

        return fragmentBinding.root
    }

    private fun initializeDisplayValues() {

        fragmentBinding.swExpandOneCategory.isChecked =
            SettingsManager.getSetting(SettingId.EXPAND_ONE_CATEGORY) as Boolean

        fragmentBinding.swCollapseCheckedSublists.isChecked =
            SettingsManager.getSetting(SettingId.COLLAPSE_CHECKED_SUBLISTS) as Boolean

        fragmentBinding.swCloseAddItemDialog.isChecked =
            SettingsManager.getSetting(SettingId.CLOSE_ITEM_DIALOG) as Boolean

        fragmentBinding.swMoveCheckedCategoriesDown.isChecked =
            SettingsManager.getSetting(SettingId.MOVE_CHECKED_DOWN) as Boolean

        fragmentBinding.swSuggestSimilarItems.isChecked =
            SettingsManager.getSetting(SettingId.SUGGEST_SIMILAR_ITEMS) as Boolean
    }

    private fun initializeListeners() {
        //changing to custom item fragment via onclick listener
        fragmentBinding.clManageCustomItems.setOnClickListener {
            (activity as MainActivity).changeToFragment(FT.CUSTOM_ITEMS)
        }

        //Switch for only showing one category as expanded
        fragmentBinding.swExpandOneCategory.setOnClickListener {
            SettingsManager.addSetting(SettingId.EXPAND_ONE_CATEGORY, fragmentBinding.swExpandOneCategory.isChecked)
        }

        //Switch to collapse sublists when they are fully checked
        fragmentBinding.swCollapseCheckedSublists.setOnClickListener {
            SettingsManager.addSetting(
                SettingId.COLLAPSE_CHECKED_SUBLISTS,
                fragmentBinding.swCollapseCheckedSublists.isChecked
            )
        }

        //Switch to close item dialog after adding a single item
        fragmentBinding.swCloseAddItemDialog.setOnClickListener {
            SettingsManager.addSetting(
                SettingId.CLOSE_ITEM_DIALOG,
                fragmentBinding.swCloseAddItemDialog.isChecked
            )
        }

        //Switch to toggle setting to move categories below unchecked lists once they are fully checked
        fragmentBinding.swMoveCheckedCategoriesDown.setOnClickListener {
            SettingsManager.addSetting(
                SettingId.MOVE_CHECKED_DOWN,
                fragmentBinding.swMoveCheckedCategoriesDown.isChecked
            )
        }

        //Switch to toggle setting to suggest similar items when adding items to shopping list with unknown names
        fragmentBinding.swSuggestSimilarItems.setOnClickListener {
            SettingsManager.addSetting(
                SettingId.SUGGEST_SIMILAR_ITEMS,
                fragmentBinding.swSuggestSimilarItems.isChecked
            )
            ShoppingFr.suggestSimilar = fragmentBinding.swSuggestSimilarItems.isChecked
        }
    }
}
