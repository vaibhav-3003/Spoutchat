package com.example.spoutchat

import android.app.Application
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SpoutChat: Application() {

    override fun onCreate() {
        // For offline capabilities of firebase
        super.onCreate()
        Firebase.database.setPersistenceEnabled(true)
    }

}