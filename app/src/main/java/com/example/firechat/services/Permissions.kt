package com.example.firechat.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.firechat.R
import com.google.android.material.snackbar.Snackbar

const val READ_PERMISSION_REQ_CODE = 4
const val CAMERA_PERMISSION_REQ_CODE = 6

//Used to check if application have needed permissions
fun checkPermission(context: Context, reqCode: Int): Boolean {
    when(reqCode){
        READ_PERMISSION_REQ_CODE -> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            return true
        }
        CAMERA_PERMISSION_REQ_CODE -> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            return true
        }
        else -> return false
    }
}

//Used if application doesn't have the needed permissions to ask for it
fun requestPermissionForRead(context: Context): Boolean {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            Snackbar.make(context.findViewById<View>(android.R.id.content), context.getString(R.string.permission_read_explanation), Snackbar.LENGTH_INDEFINITE)
                .setAction(context.getString(R.string.ok)) { ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_PERMISSION_REQ_CODE)}
                .show()
            return false
        }
        ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_PERMISSION_REQ_CODE)
        return false
    }
    return true
}

fun requestPermissionForCamera(context: Context): Boolean {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.CAMERA)) {

            Snackbar.make(context.findViewById<View>(android.R.id.content), context.getString(R.string.permission_camera_explanation), Snackbar.LENGTH_INDEFINITE)
                .setAction(context.getString(R.string.ok)) { ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQ_CODE)}
                .show()
            return false
        }
        ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQ_CODE)
        return false
    }
    return true
}