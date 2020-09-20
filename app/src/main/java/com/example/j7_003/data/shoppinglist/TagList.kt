package com.example.j7_003.data.shoppinglist

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList
import com.example.j7_003.MainActivity.Companion.act as mainContext

class TagList : ArrayList<Tag>() {

    init {
        loadFromStaticList()
    }

    fun getTagByName(name: String): Tag {
        this.forEach { e ->
            if (e.name.toLowerCase(Locale.ROOT) == name.toLowerCase(Locale.ROOT)) {
                return e
            }
        }
        throw NullPointerException("Searching for wrong tag")
    }

    fun getTagNames(): Array<String?>{
        val a: Array<String?> = arrayOfNulls(this.size)
        this.forEach {
            a[this.indexOf(it)] = it.name
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