package com.example.firechat.models

import android.widget.ImageView
import java.util.*

data class Message (
    var id: String = "",
    var senderName: String? = "",
    var avatarUrl: String? = "",
    var photoUrl: String? = "",
    var text: String? = "",
    var date: Date? = null
) {

}