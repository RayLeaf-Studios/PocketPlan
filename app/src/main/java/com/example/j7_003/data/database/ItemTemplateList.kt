package com.example.j7_003.data.database

import android.util.Log
import com.example.j7_003.MainActivity
import com.example.j7_003.data.database.database_objects.Tag
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.NullPointerException
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
            if (e.name.toLowerCase(Locale.ROOT) == name.toLowerCase(Locale.ROOT)) {
                return e
            }
        }

        return null
    }

    private fun loadFromAssets() {
        val jsonString =    // TODO set file name to production name
            MainActivity.myActivity.assets.open("testList.json").bufferedReader().readText()

        val list: ArrayList<TMPTemplate> = GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<ArrayList<TMPTemplate>>() {}.type
        )

        val tagList = TagList()
        list.forEach { e ->
            if (tagList.getTagByName(e.category) != null) {
                this.add(ItemTemplate(e.name, tagList.getTagByName(e.category)!!, e.suggestedUnit))
            }
        }
    }

    private class TMPTemplate(val name: String, val category: String, val suggestedUnit: String)
}

data class ItemTemplate(val name: String, val category: Tag, val suggestedUnit: String)
