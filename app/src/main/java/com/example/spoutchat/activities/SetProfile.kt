package com.example.spoutchat.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.example.spoutchat.R
import com.example.spoutchat.utils.Util
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import jp.wasabeef.glide.transformations.BlurTransformation


class SetProfile : AppCompatActivity() {
    private var storagePath:String? = null
    private var name:String? = null
    private var status:String? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null
    private lateinit var userImage:ImageView
    private lateinit var bgBlurImage:ImageView
    private lateinit var doneBtn:Button
    private lateinit var username:TextInputLayout
    private lateinit var statusTextField: TextInputLayout
    private lateinit var progressBarOfSetProfile:ProgressBar
    private var phoneNumber:String? = null
    private var countryCode:String? = null
    private var uID:String? = null
    private val util = Util()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_profile)

        userImage = findViewById(R.id.userImage)
        bgBlurImage = findViewById(R.id.bgBlurImage)
        doneBtn = findViewById(R.id.doneBtn)
        username = findViewById(R.id.username)
        statusTextField = findViewById(R.id.status)
        progressBarOfSetProfile = findViewById(R.id.progressBarOfSetProfile)
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        storageReference = FirebaseStorage.getInstance().reference
        storagePath = firebaseAuth.uid+"Media/Profile_Image/profile"
        phoneNumber = intent.getStringExtra("phoneNumber")
        countryCode = intent.getStringExtra("countryCode")
        uID = firebaseAuth.currentUser?.uid

        userImage.setOnClickListener{

            if(isStoragePermissionOk()){
                pickImage()
            }
        }

        doneBtn.setOnClickListener {
            if(checkName() && checkImage()){
                hideSoftKeyboard(it)
                progressBarOfSetProfile.visibility = View.VISIBLE
                status = statusTextField.editText?.text.toString()
                if(status!!.isEmpty()){
                    status = "Hey ! I am using spoutchat"
                }
                uploadData()
            }
        }

        //transparent status bar
        val window = this.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT

    }

    private fun uploadData() {

        imageUri?.let {
            storagePath?.let { it1 ->
                storageReference.child(it1).putFile(it).addOnSuccessListener { taskSnapshot ->
                    val task: Task<Uri> = taskSnapshot.storage.downloadUrl
                    task.addOnCompleteListener { task ->
                        val url = task.result.toString()
                        val map: MutableMap<String, Any> = HashMap()
                        map["uID"] = uID!!
                        map["name"] = name!!
                        map["number"] = phoneNumber!!
                        map["status"] = status!!
                        map["image"] = url

                        firebaseAuth.uid?.let { it2 -> databaseReference.child(it2).updateChildren(map).addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                progressBarOfSetProfile.visibility = View.INVISIBLE
                                val intent = Intent(this@SetProfile,DashBoard::class.java)
                                startActivity(intent)
                                finish()
                            }else{
                                Toast.makeText(applicationContext,"Failed to upload data",Toast.LENGTH_SHORT).show()
                            }
                        } }
                    }
                }
            }
        }

    }

    private fun isStoragePermissionOk():Boolean{
        return if(ActivityCompat.checkSelfPermission(applicationContext,android.Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            true
        } else{
            requestStoragePermission()
            false
        }
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            100
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage()
            } else {
                Toast.makeText(applicationContext, "Permission denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImage() {
        CropImage.activity()
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                imageUri = result.uri
                userImage.setImageURI(imageUri)
                Glide.with(this)
                    .load(imageUri)
                    .apply(bitmapTransform(BlurTransformation(20, 3)))
                    .into(bgBlurImage)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun checkName():Boolean{
        name = username.editText?.text.toString()
        return if(name!!.isEmpty()){
            username.error = "Field is required"
            false
        }else{
            username.error = null
            true
        }
    }

    private fun checkImage():Boolean{
        return if(imageUri==null){
            Toast.makeText(applicationContext,"Image is required",Toast.LENGTH_SHORT).show()
            false
        }else{
            true
        }

    }

    private fun hideSoftKeyboard(view: View) {
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
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