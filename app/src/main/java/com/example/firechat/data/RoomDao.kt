package com.example.firechat.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage

//Data layer to handle connection with firebase services
class RoomDao {

    private val db = FirebaseFirestore.getInstance()
    private val roomsRef = db.collection("chatrooms")

    companion object {
        const val TAG = "RoomDao"
    }

    //Sets up a reference to get all rooms from cloud store
    fun getAllRooms(): Task<QuerySnapshot> {
        val docRef = roomsRef
        Log.d(TAG, "GetAll: chatrooms")
        return docRef.get()
    }

    //Sets up a reference to the listener waiting for room updates
    fun roomListener(): CollectionReference {
        val docRef = roomsRef
        Log.d(TAG, "roomListener: chatrooms")
        return docRef
    }

    //Sets up a reference when needed to update room data
    fun updateRoom(): CollectionReference {
        val docRef = roomsRef
        Log.d(TAG, "roomListener: chatrooms")
        return docRef
    }
}