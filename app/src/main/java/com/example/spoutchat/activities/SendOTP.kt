package com.example.spoutchat.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.spoutchat.R
import com.example.spoutchat.utils.Util
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.hbb20.CountryCodePicker
import java.util.concurrent.TimeUnit

class SendOTP : AppCompatActivity() {
    lateinit var number:EditText
    lateinit var generateOTPBtn: Button
    lateinit var firebaseAuth:FirebaseAuth
    lateinit var countryCodePicker:CountryCodePicker
    lateinit var progressBarOfSendOTP:ProgressBar
    lateinit var countryCode:String
    private val util = Util()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_otp)

        number = findViewById(R.id.number)
        generateOTPBtn = findViewById(R.id.generateOTPBtn)
        firebaseAuth = FirebaseAuth.getInstance()
        countryCodePicker = findViewById(R.id.cpp)
        progressBarOfSendOTP = findViewById(R.id.progressBarOfSendOTP)
        countryCode = countryCodePicker.selectedCountryCodeWithPlus



        generateOTPBtn.setOnClickListener {
            if (checkNumber()) {
                util.hideKeyBoard(this)
                progressBarOfSendOTP.visibility = View.VISIBLE
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                val phoneNumber: String = countryCode + number.text.toString()
                sendVerificationCode(phoneNumber)
            }

        }


        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = this.resources.getColor(R.color.primary)
        window.navigationBarColor = this.resources.getColor(R.color.primaryVariant)
    }
    private fun checkNumber():Boolean{
        number = findViewById(R.id.number)
        return if(number.text.toString().isEmpty()){
            number.error = "Field is required"
            false
        }else if(number.text.toString().length<10){
            number.error = "Invalid number"
            false
        }else{
            number.error = null
            true
        }
    }
    private fun sendVerificationCode(phoneNumber:String){
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(this@SendOTP)
            .setCallbacks(
                object : OnVerificationStateChangedCallbacks() {

                    override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                        //This function is for automatically fetch the OTP from your Mobile.
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Toast.makeText(applicationContext,"Verification failed",Toast.LENGTH_SHORT).show()
                        progressBarOfSendOTP.visibility = View.GONE
                    }

                    //This function runs if the OTP is sent successfully
                    override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                        super.onCodeSent(s, forceResendingToken)
                        val codeSent:String = s
                        Toast.makeText(applicationContext, "OTP is Send Successfully", Toast.LENGTH_SHORT)
                            .show()
                        progressBarOfSendOTP.visibility = View.GONE
                        val intent = Intent(this@SendOTP, VerifyOTP::class.java)
                        intent.putExtra("otp",codeSent)
                        intent.putExtra("countryCode",countryCode)
                        intent.putExtra("phoneNumber",number.text.toString())
                        startActivity(intent)
                        finish()
                    }
                }
            )
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        progressBarOfSendOTP.visibility = View.GONE
    }
    override fun onResume() {
        util.updateOnlineStatus("online")
        super.onResume()
    }

    override fun onPause() {
        util.updateOnlineStatus(System.currentTimeMillis().toString())
        util.hideKeyBoard(this)
        super.onPause()
    }

}