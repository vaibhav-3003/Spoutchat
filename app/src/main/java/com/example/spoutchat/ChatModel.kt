package com.example.spoutchat

class ChatModel {

    private var chatID: String? = null
    private var name:String? = null
    private var lastMessage:String? = null
    private var image:String? = null
    private var date:String? = null
    private var online:String? = null

    constructor() {}

    constructor(
        chatID: String?,
        name: String,
        lastMessage: String,
        image: String,
        date: String,
        online: String
    ) {
        this.chatID = chatID
        this.name = name
        this.lastMessage = lastMessage
        this.image = image
        this.date = date
        this.online = online
    }

    fun getOnline(): String? {
        return online
    }

    fun setOnline(online: String) {
        this.online = online
    }

    fun getChatID(): String? {
        return chatID
    }

    fun setChatID(chatID: String?) {
        this.chatID = chatID
    }

    fun getName(): String? {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }

    fun getLastMessage(): String? {
        return lastMessage
    }

    fun setLastMessage(lastMessage: String) {
        this.lastMessage = lastMessage
    }

    fun getImage(): String? {
        return image
    }

    fun setImage(image: String) {
        this.image = image
    }

    fun getDate(): String? {
        return date
    }

    fun setDate(date: String) {
        this.date = date
    }

}