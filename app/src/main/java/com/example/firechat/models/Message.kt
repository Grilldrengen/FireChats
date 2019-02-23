package com.example.firechat.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Message (
    var id: String = "",
    var senderName: String? = "",
    var avatarUrl: String? = "",
    var photoUrl: String? = "",
    var text: String? = "",
    @ServerTimestamp var date: Date? = null
)