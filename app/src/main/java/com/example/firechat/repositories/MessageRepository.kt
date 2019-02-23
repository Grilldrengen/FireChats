package com.example.firechat.repositories

import com.example.firechat.data.MessageDao

class MessageRepository() {
    private val messageDao: MessageDao = MessageDao()

    val addMessage = messageDao.addMessage()

    val messageListener = messageDao.messageListener()

    val addImage = messageDao.addImage
}