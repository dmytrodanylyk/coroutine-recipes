package com.dmytrodanylyk

import android.app.Application

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        System.setProperty("kotlinx.coroutines.debug", if (BuildConfig.DEBUG) "on" else "off")
    }
}