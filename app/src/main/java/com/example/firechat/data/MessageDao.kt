package com.example.firechat.data

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MessageDao {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.getReferenceFromUrl("gs://firechat-93fda.appspot.com/")
    private val messagesRef = db.collection("messages")

    companion object {
        const val TAG = "MessageDao"
    }

    fun addMessage(): CollectionReference {
        val docRef = messagesRef
        Log.d(TAG, "Add: messages")
        return docRef
    }

    fun messageListener(): CollectionReference {
        val docRef = messagesRef
        Log.d(TAG, "messageListener: messages")
        return docRef
    }

    val addImage: StorageReference = storageRef
}