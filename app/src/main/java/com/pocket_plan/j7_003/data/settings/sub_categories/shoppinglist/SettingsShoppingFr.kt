package com.pocket_plan.j7_003.data.settings.sub_categories.shoppinglist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.databinding.FragmentSettingsShoppingBinding
import com.pocket_plan.j7_003.system_interaction.handler.storage.PreferencesHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

/**
 * A simple [Fragment] subclass.
 */
class SettingsShoppingFr() :
    Fragment() {

    private val preferencesHandler: PreferencesHandler by inject()

    private var _fragmentBinding: FragmentSettingsShoppingBinding? = null
    private val fragmentBinding get() = _fragmentBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentBinding = FragmentSettingsShoppingBinding.inflate(inflater, container, false)

        runBlocking {
            initializeDisplayValues()
            initializeListeners()
        }

        return fragmentBinding.root
    }

    private suspend fun initializeDisplayValues() {

        fragmentBinding.swExpandOneCategory.isChecked =
            preferencesHandler.read(PreferencesHandler.EXPAND_ONE_CATEGORY).first()

        fragmentBinding.swCollapseCheckedSublists.isChecked =
            preferencesHandler.read(PreferencesHandler.COLLAPSE_CHECKED_SUBLISTS).first()

        fragmentBinding.swCloseAddItemDialog.isChecked =
            preferencesHandler.read(PreferencesHandler.CLOSE_ITEM_DIALOG).first()

        fragmentBinding.swMoveCheckedCategoriesDown.isChecked =
            preferencesHandler.read(PreferencesHandler.MOVE_CHECKED_DOWN).first()

        fragmentBinding.swSuggestSimilarItems.isChecked =
            preferencesHandler.read(PreferencesHandler.SUGGEST_SIMILAR_ITEMS).first()
    }

    private fun initializeListeners() {
        //changing to custom item fragment via onclick listener
        fragmentBinding.clManageCustomItems.setOnClickListener {
            (activity as MainActivity).changeToFragment(FT.CUSTOM_ITEMS)
        }

        //Switch for only showing one category as expanded
        fragmentBinding.swExpandOneCategory.setOnClickListener {
            runBlocking {
                preferencesHandler.save(
                    PreferencesHandler.EXPAND_ONE_CATEGORY,
                    fragmentBinding.swExpandOneCategory.isChecked
                )
            }
        }

        //Switch to collapse sublists when they are fully checked
        fragmentBinding.swCollapseCheckedSublists.setOnClickListener {
            runBlocking {
                preferencesHandler.save(
                    PreferencesHandler.COLLAPSE_CHECKED_SUBLISTS,
                    fragmentBinding.swCollapseCheckedSublists.isChecked
                )
            }
        }

        //Switch to close item dialog after adding a single item
        fragmentBinding.swCloseAddItemDialog.setOnClickListener {
            runBlocking {
                preferencesHandler.save(
                    PreferencesHandler.CLOSE_ITEM_DIALOG,
                    fragmentBinding.swCloseAddItemDialog.isChecked
                )
            }
        }

        //Switch to toggle setting to move categories below unchecked lists once they are fully checked
        fragmentBinding.swMoveCheckedCategoriesDown.setOnClickListener {
            runBlocking {
                preferencesHandler.save(
                    PreferencesHandler.MOVE_CHECKED_DOWN,
                    fragmentBinding.swMoveCheckedCategoriesDown.isChecked
                )
            }
        }

        //Switch to toggle setting to suggest similar items when adding items to shopping list with unknown names
        fragmentBinding.swSuggestSimilarItems.setOnClickListener {
            runBlocking {
                preferencesHandler.save(
                    PreferencesHandler.SUGGEST_SIMILAR_ITEMS,
                    fragmentBinding.swSuggestSimilarItems.isChecked
                )
            }
        }
    }
}
