package com.example.j7_003.data.database

import com.example.j7_003.data.database.database_objects.Tag
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList
import com.example.j7_003.MainActivity.Companion.act as mainContext

class TagList : ArrayList<Tag>() {

    init {
        loadFromStaticList()
    }

    fun getTagByName(name: String): Tag? {
        this.forEach { e ->
            if (e.n.toLowerCase(Locale.ROOT) == name.toLowerCase(Locale.ROOT)) {
                return e
            }
        }

        return null
    }

    fun getTagNames(): Array<String?>{
        val a: Array<String?> = arrayOfNulls(this.size)
        this.forEach {
            a[this.indexOf(it)] = it.n
        }
        return a
    }

    private fun loadFromStaticList() {
        val jsonString = mainContext.assets.open("Tags.json").bufferedReader().readText()

        //todo this throws error when adding item due to wrong json?!?!
        this.addAll(GsonBuilder().create()
            .fromJson(jsonString, object : TypeToken<ArrayList<Tag>>() {}.type))
    }
}