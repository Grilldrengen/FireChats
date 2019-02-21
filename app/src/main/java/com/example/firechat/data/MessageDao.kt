package com.example.firechat.data

import android.util.Log
import com.example.firechat.models.Message
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*

class MessageDao {

    private val db = FirebaseFirestore.getInstance()
    private val messagesRef = db.collection("messages")

    companion object {
        const val TAG = "MessageDao"
    }

    fun addMessage(): CollectionReference {
        val docRef = messagesRef
        Log.d(TAG, "Add: messages")
        return docRef
    }

    fun getAllMessages(): CollectionReference {
        val docRef = messagesRef
        Log.d(TAG, "GetAll: messages")
        return docRef
    }

    fun messageListener(): CollectionReference {
        val docRef = messagesRef
        Log.d(TAG, "messageListener: messages")
        return docRef
    }
}