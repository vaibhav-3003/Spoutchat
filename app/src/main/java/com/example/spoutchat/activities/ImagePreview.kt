package com.example.spoutchat.activities


import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.*
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.spoutchat.R
import com.example.spoutchat.databinding.ActivityImagePreviewBinding
import com.google.android.material.appbar.AppBarLayout


class ImagePreview : AppCompatActivity() {

    private var binding:ActivityImagePreviewBinding? = null
    private var image:String? = null
    private var date:String? = null
    private var toolbarShown = false
    private val hideHandler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable {
        hideToolbar()
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.activity_image_preview,
            null,
            false
        )
        setContentView(binding!!.root)


        if (intent.hasExtra("image") && intent.hasExtra("date")) {
            image = intent.getStringExtra("image")
            date = intent.getStringExtra("date")
            binding!!.image = image
        }

        // Hide the status bar and navigation bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        setSupportActionBar(binding!!.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = date.toString()

        // Hide the appBar initially
        binding!!.appbar.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Remove the listener to avoid redundant calls
                binding!!.appbar.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // Set the initial position of the AppBarLayout to be hidden
                binding!!.appbar.translationY = -binding!!.appbar.height.toFloat()
            }
        })

        Glide.with(this)
            .load(image)
            .into(object : CustomTarget<Drawable>(){
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    binding!!.imagePreview.setImageDrawable(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }

            })

        binding!!.imagePreview.setOnClickListener {
            if(toolbarShown){
                hideToolbar()
            }else{
                showToolbar()
            }
        }
    }
    private fun showToolbar() {
        if (!toolbarShown) {
            val appBar = findViewById<AppBarLayout>(R.id.appbar)

            appBar?.animate()?.translationY(0f)?.setDuration(200)?.start()
            toolbarShown = true
            hideHandler.postDelayed(hideRunnable, 3000)

        }
    }

    private fun hideToolbar() {
        if (toolbarShown) {
            val appBar = findViewById<AppBarLayout>(R.id.appbar)

            appBar?.animate()?.translationY(-appBar.height.toFloat())?.setDuration(200)?.start()
            toolbarShown = false

        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}