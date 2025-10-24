package com.pocket_plan.j7_003.data.shoppinglist.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pocket_plan.j7_003.data.shoppinglist.ShoppingListWrapper
import com.pocket_plan.j7_003.data.shoppinglist.views.components.ShoppingListView
import kotlinx.coroutines.launch

@Composable
fun MultiShoppingView(modifier: Modifier = Modifier, wrapper: ShoppingListWrapper) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { wrapper.size })
    val currentPage = remember { derivedStateOf { pagerState.currentPage } }

    Column {
        PrimaryTabRow(
            modifier = modifier,
            selectedTabIndex = currentPage.value,
        ) {
            wrapper.forEachIndexed { index, (name, _) ->
                Tab(
                    selected = currentPage.value == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(name)
                    },
                )
            }
        }
        HorizontalPager(
            modifier = Modifier.fillMaxHeight(),
            state = pagerState
        ) {
            Box(modifier = Modifier.padding(10.dp)) {
                ShoppingListView(wrapper[it].second)
            }
        }
    }
}
