package com.example.spoutchat.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.spoutchat.BottomNavigationViewWithIndicator
import com.example.spoutchat.R
import com.example.spoutchat.fragments.ChatFragment
import com.example.spoutchat.fragments.ContactFragment
import com.example.spoutchat.fragments.ProfileFragment
import com.example.spoutchat.permissions.Permissions
import com.example.spoutchat.utils.Util

class DashBoard : AppCompatActivity() {

    private lateinit var navigationBar:BottomNavigationViewWithIndicator
    private var fragment:Fragment? = null
    private val util = Util()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)

        navigationBar = findViewById(R.id.bottomNavigation)

        if(savedInstanceState==null){
            supportFragmentManager.beginTransaction().replace(R.id.dashboardContainer,ChatFragment()).commit()
            window.statusBarColor = this.resources.getColor(R.color.primary)
        }

        navigationBar.apply {
                setOnNavigationItemSelectedListener { item ->
                    when(item.itemId){
                        R.id.chat -> {
                            fragment = ChatFragment()
                            window.decorView.systemUiVisibility = View.VISIBLE
                            window.statusBarColor = this.resources.getColor(R.color.primary)
                        }
                        R.id.profile -> {
                            fragment = ProfileFragment()
                            //transparent status bar
                            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            window.statusBarColor = Color.TRANSPARENT
                        }
                        R.id.contacts -> {
                            fragment = ContactFragment()
                            window.decorView.systemUiVisibility = View.VISIBLE
                            window.statusBarColor = this.resources.getColor(R.color.primary)
                        }
                    }
                    if (fragment!=null){
                        supportFragmentManager.beginTransaction().replace(R.id.dashboardContainer,
                            fragment!!
                        ).commit()
                    }
                    true
                }
        }

        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.navigationBarColor = this.resources.getColor(R.color.primary)
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


