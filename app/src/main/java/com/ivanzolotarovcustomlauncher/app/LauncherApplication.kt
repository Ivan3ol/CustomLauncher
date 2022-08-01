package com.ivanzolotarovcustomlauncher.app

import android.app.Application

//Class to get Application instance
class LauncherApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: LauncherApplication
    }
}