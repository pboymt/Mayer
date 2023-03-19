package icu.pboymt.mayer

import android.app.Application

//import android.os.StrictMode

class MayerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

//        if (BuildConfig.DEBUG)
//            StrictMode.enableDefaults()

        Thread.setDefaultUncaughtExceptionHandler { t, e ->

        }
    }
}