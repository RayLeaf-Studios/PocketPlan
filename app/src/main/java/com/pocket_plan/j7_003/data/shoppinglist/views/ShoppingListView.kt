package com.pocket_plan.j7_003.data.shoppinglist.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.shoppinglist.ShoppingList
import com.pocket_plan.j7_003.data.shoppinglist.views.components.Category

@Composable
fun ShoppingListView(shoppingList: ShoppingList, modifier: Modifier = Modifier) {
    val categoryCodes = stringArrayResource(R.array.categoryCodes)

    LazyColumn(
        modifier = modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(shoppingList) { item ->
            if (item.first !in categoryCodes) return@items

            Category(item.first, item.second)
        }
        item {
            Box(modifier = Modifier.padding(bottom = 60.dp))
        }
    }
}