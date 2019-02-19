package com.example.firechat.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot

class RoomDao() {

    private val db = FirebaseFirestore.getInstance()
    private val roomsRef = db.collection("chatrooms")

    companion object {
        const val TAG = "RoomDao"
    }

    fun getAllRooms(): Task<QuerySnapshot> {
        val docRef = roomsRef
        Log.d(TAG, "Checking CollectionReference exists for: chatrooms")
        return docRef.get()
    }

    fun roomListener(): CollectionReference {
        val docRef = roomsRef
        Log.d(TAG, "Checking CollectionReference exists for: chatrooms")
        return docRef
    }
}