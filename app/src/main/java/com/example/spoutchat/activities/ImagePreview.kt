package com.example.spoutchat.activities

import android.R.attr.previewImage
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.spoutchat.R
import com.example.spoutchat.databinding.ActivityImagePreviewBinding


class ImagePreview : AppCompatActivity() {

    private var binding:ActivityImagePreviewBinding? = null
    private var image:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(LayoutInflater.from(this),R.layout.activity_image_preview,null,false)
        setContentView(binding!!.root)

        if(intent.hasExtra("image")){
            val bitmap: Bitmap? = intent.getParcelableExtra("image")
            binding!!.imagePreview.setImageBitmap(bitmap)
        }else{
            println("Image is empty")
        }

    }
}