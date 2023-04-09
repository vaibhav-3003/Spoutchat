package com.example.spoutchat

class MessageModel {

    private var sender:String? = null
    private var receiver:String? = null
    private var message:String? = null
    private var date:String? = null
    private var type:String? = null

    constructor() {}

    constructor(
        sender: String?,
        receiver: String,
        message: String,
        date: String,
        type: String
    ) {
        this.sender = sender
        this.receiver = receiver
        this.message = message
        this.date = date
        this.type = type
    }

    fun getSender(): String? {
        return sender
    }

    fun setSender(sender: String?) {
        this.sender = sender
    }

    fun getReceiver(): String? {
        return receiver
    }

    fun setReceiver(receiver: String) {
        this.receiver = receiver
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String) {
        this.message = message
    }

    fun getDate(): String? {
        return date
    }

    fun setDate(date: String) {
        this.date = date
    }

    fun getType(): String? {
        return type
    }

    fun setType(type: String) {
        this.type = type
    }

}