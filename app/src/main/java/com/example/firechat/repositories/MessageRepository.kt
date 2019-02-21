package com.example.firechat.repositories

import com.example.firechat.data.MessageDao
import com.example.firechat.models.Message
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class MessageRepository() {
    private val messageDao: MessageDao = MessageDao()

    val allMessages = messageDao.getAllMessages()

    val addMessage = messageDao.addMessage()

    //fun messageListener(id: String) = messageDao.messageListener(id)
    val messageListener = messageDao.messageListener()
}