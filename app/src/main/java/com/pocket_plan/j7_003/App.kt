package com.pocket_plan.j7_003

import android.app.Application
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager

class App: Application() {

    companion object{
        lateinit var instance: App private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}