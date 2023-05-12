package com.pocket_plan.j7_003

import android.app.Application

class App: Application() {

    companion object{
        lateinit var instance: App private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}