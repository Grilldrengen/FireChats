package com.example.firechat.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

//Message object user to store messages from firebase cloud store and saving new ones in cloud store.
data class Room  (
    var id: String = "",
    var name: String? = "",
    var description: String? = "",
    @ServerTimestamp var lastMessage: Date? = null
): Comparable<Room>{

    //Used to compare the rooms latest message time. This makes it easy and fast to sort the rooms,
    //so the room with the last received messages is placed at the top of the list.
    override fun compareTo(other: Room): Int {
        return lastMessage?.compareTo(other.lastMessage) ?: 0
    }

    //Const used multiple places to secure we only need to change the text in one place and not multiple.
    companion object {
        const val ID = "id"
        const val NAME = "name"
        const val DESCRIPTION = "description"
        const val LASTMESSAGE = "lastMessage"
    }
}