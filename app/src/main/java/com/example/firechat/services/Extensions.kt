package com.example.firechat.services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import java.text.SimpleDateFormat
import java.util.*

//Makes inflating the view easier and more readable
fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

//Makes it easier to format dates in code
fun datetime(date: Date?): String {
    val dateFormate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.forLanguageTag("da_DK"))
    return dateFormate.format(date)
}