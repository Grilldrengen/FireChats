package com.example.firechat.repositories

import com.example.firechat.data.RoomDao
import com.example.firechat.models.Room
import com.example.firechat.services.MutablePair
import com.google.firebase.firestore.ListenerRegistration

class RoomRepository {
    private val roomDao: RoomDao = RoomDao()

    val allRooms: MutableList<Room> = roomDao.getAllRooms()

    val roomListener: MutablePair<ListenerRegistration, MutableList<Room>> = roomDao.roomListener()

}