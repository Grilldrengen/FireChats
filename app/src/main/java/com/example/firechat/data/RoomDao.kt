package com.example.firechat.data

import android.util.Log
import com.example.firechat.models.Room
import com.example.firechat.services.MutablePair
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class RoomDao() {

    private val db = FirebaseFirestore.getInstance()
    private var roomList: MutableList<Room> = mutableListOf()
    private lateinit var firestoreListener: ListenerRegistration

    companion object {
        const val TAG = "RoomDao"
    }

    fun getAllRooms(): MutableList<Room> {
        db.collection("chatrooms")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    roomList = mutableListOf<Room>()

                    for (doc in task.result!!) {
                        val room = doc.toObject(Room::class.java)
                        room.id = doc.id
                        room.name = doc.getString("Name")
                        room.description = doc.getString("Description")
                        roomList.add(room)

                        Log.d(TAG, "---------------Roomxz----${room.id}-----${room.name}----${room.description}")
                    }

                    Log.d(TAG, "---------------Roomxz----${roomList.count()}-----")

                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                }
            }
        Log.d(TAG, "---------------Roomxz----${roomList.count()}-----")

        return roomList
    }

    fun roomListener(): MutablePair<ListenerRegistration, MutableList<Room>> {
        firestoreListener = db.collection("chatrooms")
            .addSnapshotListener(EventListener { documentSnapshots, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed!", e)
                    return@EventListener
                }

                roomList = mutableListOf<Room>()

                for (doc in documentSnapshots!!) {
                    val room = doc.toObject(Room::class.java)
                    room.id = doc.id
                    room.name = doc.getString("Name")
                    room.description = doc.getString("Description")
                    roomList.add(room)
                }
            })

        return MutablePair(firestoreListener, roomList)
    }

}