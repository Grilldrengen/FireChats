package com.example.firechat.data

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

//Data layer to handle connection with firebase services
class MessageDao {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.getReferenceFromUrl("gs://firechat-93fda.appspot.com/")
    private val messagesRef = db.collection("messages")

    companion object {
        const val TAG = "MessageDao"
    }

    //Sets up reference to add a message
    fun addMessage(): CollectionReference {
        val docRef = messagesRef
        Log.d(TAG, "Add: messages")
        return docRef
    }

    //Sets up a reference to the listener waiting for room updates
    fun messageListener(): CollectionReference {
        val docRef = messagesRef
        Log.d(TAG, "messageListener: messages")
        return docRef
    }

    //Sets up reference to add a image to the storage
    val addImage: StorageReference = storageRef
}