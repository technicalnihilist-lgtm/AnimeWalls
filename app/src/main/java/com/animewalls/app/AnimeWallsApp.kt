package com.animewalls.app

import android.app.Application
import com.animewalls.app.di.ServiceLocator

class AnimeWallsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
