package com.example.spoutchat

class ChatListModel {

    private var chatListID: String? = null
    private var date:String? = null
    private var lastMessage:String? = null
    private var member:String? = null

    constructor(){}

    constructor(
        chatListID: String?,
        date: String?,
        lastMessage: String?,
        member: String?
    ) {
        this.chatListID = chatListID
        this.date = date
        this.lastMessage = lastMessage
        this.member = member
    }

    fun getChatListID(): String? {
        return chatListID
    }

    fun setChatListID(chatListID: String?) {
        this.chatListID = chatListID
    }

    fun getDate(): String? {
        return date
    }

    fun setDate(date: String?) {
        this.date = date
    }

    fun getLastMessage(): String? {
        return lastMessage
    }

    fun setLastMessage(lastMessage: String?) {
        this.lastMessage = lastMessage
    }

    fun getMember(): String? {
        return member
    }

    fun setMember(member: String?) {
        this.member = member
    }

}