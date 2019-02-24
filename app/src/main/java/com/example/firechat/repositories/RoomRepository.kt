package com.example.firechat.repositories

import com.example.firechat.data.RoomDao
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.QuerySnapshot

//Forwards det data layer methods to the activities
class RoomRepository() {
    private val roomDao: RoomDao = RoomDao()

    val allRooms: Task<QuerySnapshot> = roomDao.getAllRooms()

    val roomListener: CollectionReference = roomDao.roomListener()

    val updateRoom: CollectionReference = roomDao.updateRoom()

}