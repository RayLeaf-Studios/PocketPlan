package com.pocket_plan.j7_003.data.shoppinglist.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.shoppinglist.ShoppingList
import com.pocket_plan.j7_003.data.shoppinglist.views.components.Category

@Composable
fun ShoppingListView(shoppingList: ShoppingList, modifier: Modifier = Modifier) {

    Column(
        modifier = modifier.padding(bottom = 100.dp)
    ) {
        val categoryCodes = stringArrayResource(R.array.categoryCodes)
        val categoryNames = stringArrayResource(R.array.categoryNames)

        shoppingList.forEach {
            if (it.first !in categoryCodes) return@forEach

            val categoryName = categoryNames[categoryCodes.indexOf(it.first)]
            Category(categoryName, it.second)
        }
    }
}