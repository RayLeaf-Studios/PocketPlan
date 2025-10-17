package com.pocket_plan.j7_003

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.ksp.generated.*

class App: Application() {

    companion object{
        lateinit var instance: App private set
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(AppModule().module)
        }

        instance = this
    }

}
