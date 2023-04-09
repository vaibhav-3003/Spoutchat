package com.example.spoutchat.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.spoutchat.MessageModel
import com.example.spoutchat.R
import com.example.spoutchat.utils.Util
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class SendMediaService : Service() {

    private var builder: NotificationCompat.Builder? = null
    private var manager:NotificationManager? = null
    private var hisID:String? = null
    private var chatID:String? = null
    private var MAX_PROGRESS:Int? = null
    private val util = Util()
    private var images:ArrayList<String>? = null


    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        hisID = intent?.getStringExtra("hisID")
        chatID = intent?.getStringExtra("chatID")
        images = intent?.getStringArrayListExtra("media")
        MAX_PROGRESS = images?.size

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            createChannel()
        }
        startForeground(100,getNotification().build())

        for (a in images!!.indices) {
            uploadImage(images!![a])
            builder!!.setProgress(MAX_PROGRESS!!, a + 1, false)
            manager!!.notify(600, builder!!.build())
        }


        builder?.setContentTitle("Sending Complete")
            ?.setProgress(0,0,false)
        manager?.notify(600,builder?.build())
        stopSelf()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun getNotification():NotificationCompat.Builder{
        builder = NotificationCompat.Builder(this,"android")
            .setContentText("Sending Media")
            .setProgress(100,0,false)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_launcher_foreground)

        manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        manager?.notify(600, builder!!.build())

        return builder as NotificationCompat.Builder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(){
        val channel = NotificationChannel("android","Message",NotificationManager.IMPORTANCE_HIGH)
        channel.setShowBadge(true)
        channel.description = "Sending Media"
        manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        manager?.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun uploadImage(fileName:String){
        val storageReference = FirebaseStorage.getInstance().getReference(chatID.toString()+"/Media/"+util.getUID()+"/sent/"+System.currentTimeMillis())
        val uri = Uri.fromFile(File(fileName))
        storageReference.putFile(uri).addOnSuccessListener { taskSnapshot ->
            val task = taskSnapshot.storage.downloadUrl
            task.addOnCompleteListener { uri ->
                if(uri.isSuccessful){
                    val url:String = uri.result.toString()
                    val databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatID.toString())
                    val messageModel = MessageModel(util.getUID(),hisID.toString(),url,util.currentData().toString(),"image")
                    databaseReference.push().setValue(messageModel)
                }
            }
        }
    }

}