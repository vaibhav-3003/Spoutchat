package com.example.spoutchat.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.spoutchat.UserModel
import com.example.spoutchat.utils.Util
import com.google.firebase.database.*


class ProfileRepository {

    private val util = Util()
    private var liveData: MutableLiveData<UserModel>? = null
    private lateinit var databaseReference:DatabaseReference

    companion object{
        private var profileRepository: ProfileRepository? = null
        fun getInstance():ProfileRepository{
            return ProfileRepository().also { profileRepository = it }
        }
    }
    fun getUser():LiveData<UserModel>{
        if(liveData==null){
            liveData = MutableLiveData()
            databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            databaseReference.keepSynced(true)
            databaseReference.child(util.getUID()!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val userModel = dataSnapshot.getValue(UserModel::class.java)
                            liveData?.value = userModel
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }
        return liveData as MutableLiveData<UserModel>
    }

    fun editImage(uri: String) {
        val userModel = liveData!!.value
        databaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(util.getUID()!!)
        val map: MutableMap<String, Any> = HashMap()
        map["image"] = uri
        databaseReference.updateChildren(map).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userModel!!.setImage(uri)
                liveData!!.value = userModel
                Log.d("image", "onComplete: Image updated")
            } else Log.d("image", "onComplete: " + task.exception)
        }
    }

    fun editStatus(status: String) {
        val userModel = liveData!!.value
        databaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(util.getUID()!!)
        val map: MutableMap<String, Any> = HashMap()
        map["status"] = status
        databaseReference.updateChildren(map).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userModel!!.setStatus(status)
                liveData!!.value = userModel
                Log.d("status", "onComplete: Status Updated")
            } else Log.d("status", "onComplete: " + task.exception)
        }
    }

    fun editUsername(name: String) {
        val userModel = liveData!!.value
        databaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(util.getUID()!!)
        val map: MutableMap<String, Any> = HashMap()
        map["name"] = name
        databaseReference.updateChildren(map).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userModel!!.setName(name)
                liveData!!.value = userModel
                Log.d("name", "onComplete: Name Updated")
            } else Log.d("name", "onComplete:" + task.exception)
        }
    }

}