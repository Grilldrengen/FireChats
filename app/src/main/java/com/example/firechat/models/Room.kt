package com.example.firechat.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Room  (
    var id: String = "",
    var name: String? = "",
    var description: String? = "",
    @ServerTimestamp var lastMessage: Date? = null
): Comparable<Room>{
    override fun compareTo(other: Room): Int {
        return lastMessage!!.compareTo(other.lastMessage);
    }

}