package com.example.firechat

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    // Called when the application is starting, before any other application objects have been created.
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

    }

    // Called by the system when the device configuration changes while your component is running.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    // This is called when the overall system is running low on memory,
    override fun onLowMemory() {
        super.onLowMemory()
    }
}