package com.pocket_plan.j7_003.data.shoppinglist

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*
import com.pocket_plan.j7_003.MainActivity.Companion.act as mainContext

class TagList : ArrayList<Tag>() {

    val categoryCodes = arrayOf("So", "Ob", "Gt", "Nu", "Bw", "Km", "Kf", "Tk", "Ko", "Fr", "Gw", "Ha", "Sn", "Bz", "Dr", "Al")
    val categoryNames = arrayOf("Sonstiges", "Obst & Gemüse", "Getränke", "Nu", "Bw", "Km", "Kf", "Tk", "Ko", "Fr", "Gw", "Ha", "Sn", "Bz", "Dr", "Al")
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

//    fun tagNameForCode(code: String){
//        return when(code){
//            "O" -> "Obst & Gemüse"
//
//
//        }
//    }

    private fun loadFromStaticList() {
        val jsonString = mainContext.assets.open("Tags.json").bufferedReader().readText()

        //todo this throws error when adding item due to wrong json?!?!
        this.addAll(GsonBuilder().create()
            .fromJson(jsonString, object : TypeToken<ArrayList<Tag>>() {}.type))
    }
}