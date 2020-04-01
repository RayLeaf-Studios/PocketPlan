package com.example.j7_003.logic

import android.util.Log.d

class Database() {
    //Beim starten der App erstellt Main Activity eine Instanz von Database
    //Database lädt Daten aus File?!? irgendwie
    //stellt diese in Array Lists zur Verfügung
    //verwaltet änderungen
    //speichert Daten lokal nach änderungen
    var taskList = ArrayList<Task>()

    init {
        loadTaskList()
    }



    fun loadTaskList() {
        d("Database", "loadtasklist got called")
            //wird ersetzt durch einlesen von file / server?
        taskList = arrayListOf<Task>(
            Task("App programmie3ren", 3),
            Task("Beispie2l", 2),
            Task("Logo designen1", 1),
            Task("test2", 2)
        )
    }


}