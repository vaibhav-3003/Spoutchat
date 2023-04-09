package com.example.spoutchat.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import com.example.spoutchat.R
import com.example.spoutchat.activities.Message
import com.example.spoutchat.constants.AllConstants
import com.example.spoutchat.utils.Util
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*
import kotlin.collections.HashMap

class FirebaseNotificationService : FirebaseMessagingService() {

    private val util = Util()

    override fun onMessageReceived(message: RemoteMessage) {

        if (message.data.isNotEmpty()){

            val map:Map<String,String> = message.data
            val title = map["title"]
            val msg = map["message"]
            val hisID = map["hisID"]
            val hisImage = map["hisImage"]
            val chatID = map["chatID"]

            if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O){
                createOreoNotification(title.toString(),msg.toString(),hisID.toString(),hisImage.toString(),chatID.toString())
            }else{
                createNormalNotification(title.toString(),msg.toString(),hisID.toString(),hisImage.toString(),chatID.toString())
            }
        }else{
            println("message is empty")
        }

        super.onMessageReceived(message)
    }

    override fun onNewToken(token: String) {
        updateToken(token)
        super.onNewToken(token)
    }

    private fun updateToken(token: String){
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(util.getUID().toString())
        val map:MutableMap<String,Any> = HashMap()
        map["token"] = token
        databaseReference.updateChildren(map)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createNormalNotification(title:String, message:String, hisID:String, hisImage:String, chatID:String){
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(this,AllConstants.CHANNEL_ID)
        builder.setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(ResourcesCompat.getColor(resources,R.color.primary,null))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setSound(uri)

        // to open the activity when click the notification
        val intent = Intent(this,Message::class.java)
        intent.putExtra("chatID",chatID)
        intent.putExtra("hisID",hisID)
        intent.putExtra("hisImage",hisImage)

        val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT)

        builder.setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Random().nextInt(85-65),builder.build())
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createOreoNotification(title:String, message:String, hisID:String, hisImage:String, chatID:String){
        val channel = NotificationChannel(AllConstants.CHANNEL_ID,"Message",NotificationManager.IMPORTANCE_HIGH)
        channel.setShowBadge(true)
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.description = "Message Description"
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(this,Message::class.java)
        intent.putExtra("hisID",hisID)
        intent.putExtra("hisImage",hisImage)
        intent.putExtra("chatID",chatID)

        val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT)

        val notification = Notification.Builder(this,AllConstants.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(ResourcesCompat.getColor(resources,R.color.primary,null))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        notificationManager.notify(Random().nextInt(85-65),notification)
    }


}