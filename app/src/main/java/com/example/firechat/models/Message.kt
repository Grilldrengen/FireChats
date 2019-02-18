package com.example.firechat.models

import android.widget.ImageView
import java.util.*

data class Message (
    var senderName: String? = null,
    var senderAvatar: ImageView? = null,
    var text: String? = null,
    var date: Date? = null
)