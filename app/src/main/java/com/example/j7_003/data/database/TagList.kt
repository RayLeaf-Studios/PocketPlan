package com.example.j7_003.data.database

import android.util.Log
import com.example.j7_003.MainActivity
import com.example.j7_003.data.database.database_objects.CalendarAppointment
import com.example.j7_003.data.database.database_objects.Tag
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.example.j7_003.MainActivity.Companion.myActivity as mainContext

class TagList {
    var tagList: ArrayList<Tag> = ArrayList()

    fun init() {
        loadFromStaticList()
    }

    private fun loadFromStaticList() {
        val jsonString = mainContext.assets.open("Tags.json").bufferedReader().readText()

        tagList = GsonBuilder().create()
            .fromJson(jsonString, object : TypeToken<ArrayList<Tag>>() {}.type)
    }
}