package com.example.spoutchat.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class Permissions {

    fun isStorageOk(context: Context?):Boolean{
        return ContextCompat.checkSelfPermission(context!!,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED
    }

    fun requestStorage(activity: FragmentActivity?){
        if (activity != null) {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1000 )
        }
    }

    fun isContactOk(context: Context?):Boolean{
        return ContextCompat.checkSelfPermission(context!!,Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED
    }

    fun requestContact(activity: FragmentActivity?){
        if (activity != null) {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.READ_CONTACTS), 2000)
        }
    }

}