package com.example.spoutchat.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chaos.view.PinView
import com.example.spoutchat.R
import com.example.spoutchat.UserModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class VerifyOTP : AppCompatActivity() {

    lateinit var countryCode:String
    lateinit var phone:String
    lateinit var codeReceived:String
    private lateinit var pinView: PinView
    lateinit var verify:Button
    lateinit var progressBarOfVerifyOTP: ProgressBar
    private var enteredOTP:String? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var databaseReference: DatabaseReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_otp)

        countryCode = intent.getStringExtra("countryCode").toString()
        phone = intent.getStringExtra("phoneNumber").toString()
        codeReceived = intent.getStringExtra("otp").toString()
        pinView = findViewById(R.id.firstPinView)
        verify = findViewById(R.id.verify)
        progressBarOfVerifyOTP = findViewById(R.id.progressBarOfVerifyOTP)
        enteredOTP = pinView.text.toString()
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        verify.setOnClickListener {
            if(pinView.text!!.isEmpty()){
                Toast.makeText(applicationContext,"Enter otp",Toast.LENGTH_SHORT).show()
            }
            else{
                progressBarOfVerifyOTP.visibility = View.VISIBLE
                val credential = PhoneAuthProvider.getCredential(codeReceived,pinView.text.toString())
                println(codeReceived)
                println(enteredOTP)
                signInWithPhoneAuthCredential(credential)
            }
        }


        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = this.resources.getColor(R.color.primary)
        window.navigationBarColor = this.resources.getColor(R.color.primaryVariant)
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    FirebaseMessaging.getInstance().token
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                if (task.result != null && !TextUtils.isEmpty(task.result)) {
                                    val token: String = task.result!!
                                    val userModel = UserModel(
                                        "",
                                        "",
                                        "",
                                        firebaseAuth!!.currentUser?.phoneNumber,
                                        countryCode,
                                        firebaseAuth!!.uid,
                                        "online",
                                        "false",
                                        token
                                    )
                                    databaseReference?.child(firebaseAuth!!.uid.toString())
                                        ?.setValue(userModel)?.addOnCompleteListener { task2 ->
                                        if (task2.isSuccessful) {
                                            progressBarOfVerifyOTP.visibility = View.INVISIBLE
                                            Toast.makeText(
                                                applicationContext,
                                                "Login Success",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent =
                                                Intent(this@VerifyOTP, SetProfile::class.java)
                                            intent.putExtra("phoneNumber", phone)
                                            intent.putExtra("countryCode", countryCode)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                applicationContext,
                                                "" + task2.exception,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        progressBarOfVerifyOTP.visibility = View.INVISIBLE
                        Toast.makeText(
                            applicationContext,
                            "Login Failed due to incorrect OTP",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }



}