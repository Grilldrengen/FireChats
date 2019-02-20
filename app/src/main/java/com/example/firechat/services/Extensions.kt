package com.example.firechat.services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import java.text.SimpleDateFormat
import java.util.*

data class MutablePair<T, U>(var first: T, var second: U)

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun Datetime(): String {
    val dateFormate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.forLanguageTag("da_DK"))
    val date = dateFormate.format(Date())
    return date
}