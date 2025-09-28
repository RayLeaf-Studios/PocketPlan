package com.pocket_plan.j7_003.data.shoppinglist.mapper

import com.pocket_plan.j7_003.data.shoppinglist.ShoppingItem
import com.pocket_plan.j7_003.data.shoppinglist.mapper.ItemMapper.toCore
import com.pocket_plan.j7_003.data.shoppinglist.model.dtos.ShoppingCategoryDto

/**
 * Maps a [ShoppingCategoryDto] to a Pair of category name and list of [ShoppingItem]s.
 */
object CategoryMapper {

    /**
     * Converts a [ShoppingCategoryDto] to a Pair of category name and list of [ShoppingItem]s.
     * @receiver ShoppingCategoryDto The category to convert
     * @return Pair<String, ArrayList<ShoppingItem>> The converted category as a pair of name and items
     */
    fun ShoppingCategoryDto.toCore(): Pair<String, ArrayList<ShoppingItem>> {
        val cat = Pair(this.name, arrayListOf<ShoppingItem>())
        cat.second.addAll(this.items.map { it.toCore(this.name) })

        return cat
    }
}