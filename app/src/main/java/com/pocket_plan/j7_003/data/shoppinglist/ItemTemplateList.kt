package com.pocket_plan.j7_003.data.shoppinglist

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList

class ItemTemplateList(val context: Context) : ArrayList<ItemTemplate>() {
    init {
        loadFromAssets()
    }

    /**
     * Returns an ItemTemplate if one was defined in the assets item_list_de.json file.
     * @param name The name the ItemTemplate is supposed to have.
     * @return Returns the template if found, null otherwise.
     */
    fun getTemplateByName(name: String): ItemTemplate? {

        this.forEach { e ->
            if (e.n.equals(name, ignoreCase = true)) {
                return e
            }
        }
        return null

    }

    private fun loadFromAssets() {
        val jsonString =
            when {
                Locale.getDefault().displayLanguage == Locale.GERMAN.displayLanguage -> {
                    context.assets.open("item_list_de.json").bufferedReader().readText()
                }
                Locale.getDefault().displayLanguage.toString() == "русский" -> {
                    context.assets.open("item_list_ru.json").bufferedReader().readText()
                }
                else -> context.assets.open("item_list_en.json").bufferedReader().readText()
            }

        val list: ArrayList<TMPTemplate> = GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<ArrayList<TMPTemplate>>() {}.type)

        list.forEach { e ->
            this.add(ItemTemplate(e.n, e.c, "x"))
        }
    }

    private class TMPTemplate(val n: String, val c: String)
}

data class ItemTemplate(var n: String, var c: String, var s: String)
