package com.pocket_plan.j7_003.data.shoppinglist.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.shoppinglist.ShoppingItem
import com.pocket_plan.j7_003.ui.theme_light_alkohol_tabak
import com.pocket_plan.j7_003.ui.theme_light_alkohol_tabak_l
import com.pocket_plan.j7_003.ui.theme_light_backwaren
import com.pocket_plan.j7_003.ui.theme_light_backwaren_l
import com.pocket_plan.j7_003.ui.theme_light_backzutaten
import com.pocket_plan.j7_003.ui.theme_light_backzutaten_l
import com.pocket_plan.j7_003.ui.theme_light_drogerie_kosmetik
import com.pocket_plan.j7_003.ui.theme_light_drogerie_kosmetik_l
import com.pocket_plan.j7_003.ui.theme_light_fruehstueck
import com.pocket_plan.j7_003.ui.theme_light_fruehstueck_l
import com.pocket_plan.j7_003.ui.theme_light_getraenke
import com.pocket_plan.j7_003.ui.theme_light_getraenke_l
import com.pocket_plan.j7_003.ui.theme_light_gewuerze
import com.pocket_plan.j7_003.ui.theme_light_gewuerze_l
import com.pocket_plan.j7_003.ui.theme_light_haushalt
import com.pocket_plan.j7_003.ui.theme_light_haushalt_l
import com.pocket_plan.j7_003.ui.theme_light_konserven_fertiges
import com.pocket_plan.j7_003.ui.theme_light_konserven_fertiges_l
import com.pocket_plan.j7_003.ui.theme_light_kuehlregal_fleisch
import com.pocket_plan.j7_003.ui.theme_light_kuehlregal_fleisch_l
import com.pocket_plan.j7_003.ui.theme_light_kuehlregal_milch
import com.pocket_plan.j7_003.ui.theme_light_kuehlregal_milch_l
import com.pocket_plan.j7_003.ui.theme_light_nudel_und_getreide
import com.pocket_plan.j7_003.ui.theme_light_nudel_und_getreide_l
import com.pocket_plan.j7_003.ui.theme_light_obst_und_gemuese
import com.pocket_plan.j7_003.ui.theme_light_obst_und_gemuese_l
import com.pocket_plan.j7_003.ui.theme_light_on_background_task
import com.pocket_plan.j7_003.ui.theme_light_snacks
import com.pocket_plan.j7_003.ui.theme_light_snacks_l
import com.pocket_plan.j7_003.ui.theme_light_sonstiges
import com.pocket_plan.j7_003.ui.theme_light_sonstiges_l
import com.pocket_plan.j7_003.ui.theme_light_tiefkuehl
import com.pocket_plan.j7_003.ui.theme_light_tiefkuehl_l
import com.pocket_plan.j7_003.ui.theme_light_vegan
import com.pocket_plan.j7_003.ui.theme_light_vegan_l

@Composable
fun Category(
    categoryCode: String,
    items: MutableList<ShoppingItem>,
    onItemDelete: (ShoppingItem) -> Unit
) {
    val categoryNames = stringArrayResource(R.array.categoryNames)
    val categoryCodes = stringArrayResource(R.array.categoryCodes)
    val gradientColors = when (categoryCode) {
        categoryCodes[0] -> listOf(theme_light_sonstiges_l, theme_light_sonstiges)
        categoryCodes[1] -> listOf(theme_light_obst_und_gemuese_l, theme_light_obst_und_gemuese)
        categoryCodes[2] -> listOf(theme_light_getraenke_l, theme_light_getraenke)
        categoryCodes[3] -> listOf(theme_light_nudel_und_getreide_l, theme_light_nudel_und_getreide)
        categoryCodes[4] -> listOf(theme_light_backwaren_l, theme_light_backwaren)
        categoryCodes[5] -> listOf(theme_light_kuehlregal_milch_l, theme_light_kuehlregal_milch)
        categoryCodes[6] -> listOf(theme_light_kuehlregal_fleisch_l, theme_light_kuehlregal_fleisch)
        categoryCodes[7] -> listOf(theme_light_vegan_l, theme_light_vegan)
        categoryCodes[8] -> listOf(theme_light_tiefkuehl_l, theme_light_tiefkuehl)
        categoryCodes[9] -> listOf(theme_light_konserven_fertiges_l, theme_light_konserven_fertiges)
        categoryCodes[10] -> listOf(theme_light_fruehstueck_l, theme_light_fruehstueck)
        categoryCodes[11] -> listOf(theme_light_gewuerze_l, theme_light_gewuerze)
        categoryCodes[12] -> listOf(theme_light_haushalt_l, theme_light_haushalt)
        categoryCodes[13] -> listOf(theme_light_snacks_l, theme_light_snacks)
        categoryCodes[14] -> listOf(theme_light_backzutaten_l, theme_light_backzutaten)
        categoryCodes[15] -> listOf(theme_light_drogerie_kosmetik_l, theme_light_drogerie_kosmetik)
        categoryCodes[16] -> listOf(theme_light_alkohol_tabak_l, theme_light_alkohol_tabak)
        else -> listOf()
    }
    val brush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset.Zero,
        end = Offset.Infinite
    )

    val swipeState =
        rememberSwipeToDismissBoxState(
            SwipeToDismissBoxValue.Settled,
            SwipeToDismissBoxDefaults.positionalThreshold
        )

    val shoppingItems = remember { items }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Green),
        shape = RoundedCornerShape(dimensionResource(R.dimen.cornerRadius)),
        elevation = CardDefaults.cardElevation(dimensionResource(R.dimen.elevation))
    ) {
        Box(modifier = Modifier.background(brush)) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .background(Color.Transparent),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Text(text = "${items.size - 1}", color = Color.Black)
                    }
                    Text(
                        text = categoryNames[categoryCodes.indexOf(categoryCode)],
                        fontSize = dimensionResource(R.dimen.font_size_medium).value.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(40.dp),
                            painter = painterResource(R.drawable.ic_action_expand),
                            tint = theme_light_on_background_task,
                            contentDescription = ""
                        )
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items.forEach { item ->
                        if (item.name == null) return@forEach
                        SwipeToDismissBox(
                            state = swipeState,
                            modifier = Modifier
                                .fillMaxWidth(),
                            onDismiss = {
                                onItemDelete(item)
                            },
                            backgroundContent = { }
                        ) {
                            Item(item)
                        }
                    }
                }
            }
        }
    }
}