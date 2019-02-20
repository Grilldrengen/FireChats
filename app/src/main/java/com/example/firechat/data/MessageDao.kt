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

    fun addMessage(message: HashMap<String, Any>): Task<DocumentReference> {
        val docRef = messagesRef
        Log.d(TAG, "Add: messages")
        return docRef.add(message)
    }

    fun getAllMessages(id: String): Task<QuerySnapshot> {
        val docRef = messagesRef
        Log.d(TAG, "GetAll: messages")
        return docRef.get()
    }

    fun messageListener(id: String): Query {
        val docRef = messagesRef
        Log.d(TAG, "messageListener: messages")
        return docRef.whereEqualTo("id", id).limit(50)
    }

}