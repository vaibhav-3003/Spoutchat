package com.example.spoutchat.activities

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.example.spoutchat.R
import com.example.spoutchat.UserModel
import com.example.spoutchat.databinding.ActivityUserInfoBinding
import com.example.spoutchat.utils.Util
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserInfo : AppCompatActivity() {

    private var binding:ActivityUserInfoBinding? = null
    private val util = Util()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val uID:String = intent.getStringExtra("userID").toString()

        getUserDetail(uID)

        val window = this.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = this.resources.getColor(R.color.primaryVariant,theme)

    }

    private fun getUserDetail(uID: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uID)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                println(uID)
                if(snapshot.exists()){

                    val userModel = snapshot.getValue(UserModel::class.java)
                    binding!!.userModel = userModel

                    //set the name
                    binding!!.viewUserName.text = userModel?.getName()
                    binding!!.usernameProfile.text = userModel?.getName()



                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    override fun onResume() {
        util.updateOnlineStatus("online")
        super.onResume()
    }

    override fun onPause() {
        util.updateOnlineStatus(System.currentTimeMillis().toString())
        super.onPause()
    }
}