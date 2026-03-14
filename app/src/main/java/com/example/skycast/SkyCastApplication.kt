package com.example.skycast

import android.app.Application
import com.example.skycast.di.AppContainer

class SkyCastApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
