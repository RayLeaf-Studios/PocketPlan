package com.example.j7_003.data.shoppinglist

import com.example.j7_003.MainActivity
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList

class ItemTemplateList : ArrayList<ItemTemplate>() {
    init {
        loadFromAssets()
    }

    /**
     * Returns an ItemTemplate if one was defined in the assets Tags.json file.
     * @param name The name the ItemTemplate is supposed to have.
     * @return Returns the template if found, null otherwise.
     */
    fun getTemplateByName(name: String): ItemTemplate? {
        this.forEach { e ->
            if (e.n.toLowerCase(Locale.ROOT) == name.toLowerCase(Locale.ROOT)) {
                return e
            }
        }

        return null
    }

    private fun loadFromAssets() {
        val jsonString =    // TODO set file name to production name
            MainActivity.act.assets.open("itemList.json").bufferedReader().readText()

        val list: ArrayList<TMPTemplate> = GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<ArrayList<TMPTemplate>>() {}.type
        )

        val tagList = TagList()
        list.forEach { e ->
            this.add(ItemTemplate(e.n, tagList.getTagByName(e.c), e.s))
        }
    }

    private class TMPTemplate(val n: String, val c: String, val s: String)
}

data class ItemTemplate(var n: String, var c: Tag, var s: String)