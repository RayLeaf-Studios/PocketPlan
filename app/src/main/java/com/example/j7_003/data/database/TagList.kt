package com.example.j7_003.data.database

class TagList {
    //todo set initial capacity to size of standard tags
    var tagList: ArrayList<Tag> = ArrayList()

    data class Tag(var name: String)
}