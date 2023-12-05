package com.awning.afterglow

import android.app.Application

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        initialize(this)
    }
}