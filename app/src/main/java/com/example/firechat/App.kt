package com.example.firechat

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Exception

//Used to handle application lifecycle events
class App : Application() {

    companion object {
        private val TAG = this::class.java.simpleName
        const val UNCAUGHT_EXCEPTION_STACKTRACE = "UNCAUGHT EXCEPTION. Stacktrace: "
        const val UNCAUGHT_EXCEPTION_EXCEPTION = "UNCAUGHT EXCEPTION. Exception: "
    }

    // Called when the application is starting, before any other application objects have been created.
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        Thread.setDefaultUncaughtExceptionHandler { thread, e -> handleUncaugthException(thread, e) }
    }

    // Called by the system when the device configuration changes while your component is running.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    // This is called when the overall system is running low on memory,
    override fun onLowMemory() {
        super.onLowMemory()
    }

    //Used to catch uncaugth exceptions
    private fun handleUncaugthException(thread: Thread?, t: Throwable?){
        val stackTrace = StringWriter()
        t?.printStackTrace(PrintWriter(stackTrace))
        Log.e(TAG, UNCAUGHT_EXCEPTION_STACKTRACE + stackTrace)
        Log.e(TAG, UNCAUGHT_EXCEPTION_EXCEPTION + t as Exception)
        System.exit(1)
    }
}