package com.example.climbtime

import android.app.Application
import androidx.activity.result.ActivityResultLauncher
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class ClimbTimeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@ClimbTimeApplication)
            modules(appModule)
        }
    }
}
