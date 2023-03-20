package icu.pboymt.mayer

import android.app.Application
import android.os.StrictMode
import org.tinylog.kotlin.Logger

class MayerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            StrictMode.enableDefaults()

        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Logger.error(e) { "Uncaught exception in thread ${t.name}" }
        }
    }
}