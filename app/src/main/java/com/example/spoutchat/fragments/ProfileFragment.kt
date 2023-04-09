package com.example.spoutchat.fragments

import android.annotation.SuppressLint
import android.app.Activity.MODE_PRIVATE
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.spoutchat.R
import com.example.spoutchat.UserModel
import com.example.spoutchat.databinding.FragmentProfileBinding
import com.example.spoutchat.permissions.Permissions
import com.example.spoutchat.utils.Util
import com.example.spoutchat.viewModel.ProfileViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private var binding:FragmentProfileBinding? = null
    private var profileViewModel:ProfileViewModel? = null
    private var imageUri:Uri? = null
    private val util = Util()
    private val permissions = Permissions()

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("CutPasteId", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        profileViewModel =
            activity?.let { ViewModelProvider.AndroidViewModelFactory.getInstance(it.application).create(ProfileViewModel::class.java) }

        val observer: Observer<UserModel?> = Observer<UserModel?> { userModel ->
            binding!!.userModel = userModel
            val name:String = userModel.getName().toString()
            binding!!.usernameProfile.text = name
            binding!!.viewUserName.text = name

            //extract phone number from firebase auth
            val user = FirebaseAuth.getInstance().currentUser
            if(user!=null){
                val number:String = user.phoneNumber.toString()
                val countryCode:String = number.substring(0,3)
                binding!!.viewUserCountryCode.text = countryCode
                binding!!.viewUserNumber.text = number.substring(3)
            }

        }
        profileViewModel?.getUser()?.observe(viewLifecycleOwner,observer)

        // edit the image
        binding!!.editImage.setOnClickListener {
            if(permissions.isStorageOk(context)){
                pickImage()
            }else{
                permissions.requestStorage(activity)
            }

        }

        // edit the status
        binding!!.aboutCardView.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val view1 = LayoutInflater.from(context).inflate(R.layout.dialog_layout,null)
            builder.setView(view1)
            val alertDialog = builder.create()

            val editStatus:TextInputLayout = view1.findViewById(R.id.editUserStatus)
            val saveBtn: Button = view1.findViewById(R.id.save)
            val cancelBtn:Button = view1.findViewById(R.id.cancel)

            alertDialog.show()

            cancelBtn.setOnClickListener {
                alertDialog.dismiss()
            }

            saveBtn.setOnClickListener {
                val status:String = editStatus.editText?.text?.trim().toString()
                if(status.isEmpty()){
                    editStatus.error = "Field is Empty"
                }else{
                    editStatus.error = null
                    binding!!.viewAbout.text = status
                    profileViewModel?.editStatus(status)
                    alertDialog.dismiss()
                }
            }
        }

        // edit username
        binding!!.nameCardView.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val view1 = LayoutInflater.from(context).inflate(R.layout.dialog_layout,null)
            builder.setView(view1)
            val alertDialog = builder.create()

            alertDialog.show()

            val statusText:TextView = view1.findViewById(R.id.statusText)
            statusText.text = "Enter your name"

            val editName:TextInputLayout = view1.findViewById(R.id.editUserStatus)
            editName.hint = "Name"
            editName.counterMaxLength = 20

            val saveBtn: Button = view1.findViewById(R.id.save)
            val cancelBtn:Button = view1.findViewById(R.id.cancel)

            cancelBtn.setOnClickListener {
                alertDialog.dismiss()
            }
            saveBtn.setOnClickListener {
                val name:String = editName.editText?.text?.trim().toString()
                if(name.isEmpty()){
                    editName.error = "Field is Empty"
                }else{
                    editName.error = null
                    binding!!.viewUserName.text = name
                    profileViewModel?.editUsername(name)
                    alertDialog.dismiss()
                }
            }

        }


        return binding!!.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "super.onRequestPermissionsResult(requestCode, permissions, grantResults)",
        "androidx.fragment.app.Fragment"
    )
    )
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){

            1000 -> {
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    pickImage()
                }else{
                    Toast.makeText(context,"Storage Permission denied",Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

    private fun pickImage() {
        util.updateOnlineStatus("online")
        context?.let { it1 ->
            CropImage.activity()
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(it1,this)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        util.updateOnlineStatus("online")
        when(requestCode){
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if(resultCode==RESULT_OK){
                    imageUri = result.uri
                    binding?.showUserImage?.setImageURI(imageUri)
                    uploadImage(imageUri)
                }
            }

        }
    }

    private fun uploadImage(imageUri: Uri?) {
        val storageReference = FirebaseStorage.getInstance().getReference(util.getUID().toString()).child("Media/Profile_Image/profile")
        if (imageUri != null) {
            storageReference.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
                val task = taskSnapshot.storage.downloadUrl
                task.addOnCompleteListener { task ->
                    val uri:String = task.result.toString()
                    profileViewModel?.editImage(uri)
                }
            }
        }
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