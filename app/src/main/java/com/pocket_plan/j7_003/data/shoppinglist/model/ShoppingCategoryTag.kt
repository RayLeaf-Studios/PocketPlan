package com.pocket_plan.j7_003.data.shoppinglist.model

enum class ShoppingCategoryTag(val code: String) {
    OTHER("So"),
    FRUITS_AND_VEGETABLES("Ob"),
    DRINKS("Gt"),
    PASTA_AND_GRAINS("Nu"),
    BREAD_AND_PASTRIES("Bw"),
    DAIRY("Km"),
    MEAT("Kf"),
    VEGAN("Ve"),
    FROZEN("Tk"),
    CANNED_AND_READY_MADE("Ko"),
    BREAKFAST_AND_CO("Fr"),
    SPICES_AND_CO("Gw"),
    HOUSEHOLD("Ha"),
    SNACKS("Sn"),
    BAKING_INGREDIENTS("Bz"),
    DRUGSTORE_AND_COSMETICS("Dr"),
    ALCOHOLIC_BEVERAGES("Al");

    fun getTagByCode(code: String): ShoppingCategoryTag {
        return entries.firstOrNull { it.code == code } ?: OTHER
    }
}