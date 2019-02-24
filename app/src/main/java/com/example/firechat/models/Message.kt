package com.example.firechat.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

//Message object user to store messages from firebase cloud store and saving new ones in cloud store.
data class Message (
    var id: String = "",
    var senderName: String? = "",
    var avatarUrl: String? = "",
    var photoUrl: String? = "",
    var text: String? = "",
    @ServerTimestamp var date: Date? = null
) {
    //Const used multiple places to secure we only need to change the text in one place and not multiple.
    companion object {
        const val ID = "id"
        const val SENDERNAME = "senderName"
        const val AVATARURL = "avatarUrl"
        const val PHOTOURL = "photoUrl"
        const val TEXT = "text"
        const val DATE = "date"
    }
}