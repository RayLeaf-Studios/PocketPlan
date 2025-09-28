package com.pocket_plan.j7_003.data.shoppinglist.mapper

import com.pocket_plan.j7_003.data.shoppinglist.ShoppingList
import com.pocket_plan.j7_003.data.shoppinglist.mapper.CategoryMapper.toCore
import com.pocket_plan.j7_003.data.shoppinglist.model.dtos.ShoppingListDto

/**
 * Maps a [ShoppingListDto] to a [ShoppingList].
 */
object ListMapper {

    /**
     * Converts a [ShoppingListDto] to a [ShoppingList].
     * @receiver ShoppingListDto The list to convert
     * @return ShoppingList The converted list
     */
    fun ShoppingListDto.toCore(): ShoppingList {
        val newList = ShoppingList()

        list.forEach {
            newList.add(it.toCore())
        }

        return newList
    }
}