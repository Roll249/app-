package com.fintech

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Hilt dependency injection
 */
@HiltAndroidApp
class FintechApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize app-level configurations here
    }
}
