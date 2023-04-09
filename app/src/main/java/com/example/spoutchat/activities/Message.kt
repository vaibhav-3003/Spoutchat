package com.example.spoutchat.activities

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.spoutchat.BR
import com.example.spoutchat.ChatListModel
import com.example.spoutchat.MessageModel
import com.example.spoutchat.R
import com.example.spoutchat.constants.AllConstants
import com.example.spoutchat.databinding.ActivityMessageBinding
import com.example.spoutchat.databinding.LeftItemLayoutBinding
import com.example.spoutchat.databinding.RightItemLayoutBinding
import com.example.spoutchat.services.SendMediaService
import com.example.spoutchat.utils.Util
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil
import com.google.firebase.database.*
import org.json.JSONObject


class Message : AppCompatActivity() {

    private var binding:ActivityMessageBinding? = null
    private var hisID:String? = null
    private var hisImage:String? = null
    private var myID:String? = null
    private var chatID:String? = null
    private val util = Util()
    private var myName:String? = null
    private var myImage:String? = null
    private var selectedImages:ArrayList<String>? = null
    private var databaseReference:DatabaseReference? = null
    private var firebaseRecyclerAdapter:FirebaseRecyclerAdapter<MessageModel,ViewHolder>? = null
    private var sentImage:ImageView? = null
    private var view:View? = null

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(LayoutInflater.from(this),R.layout.activity_message,null,false)
        setContentView(binding!!.root)

//        view = LayoutInflater.from(this).inflate(R.layout.right_item_layout,null,false)
//        sentImage = view!!.findViewById(R.id.sentImage)

        myID = util.getUID()

        if(intent.hasExtra("chatID")){
            chatID = intent.getStringExtra("chatID")
            hisID = intent.getStringExtra("hisID")
            hisImage = intent.getStringExtra("hisImage")
            readMessages(chatID.toString())
        }else{
            hisID = intent.getStringExtra("hisID")
            hisImage = intent.getStringExtra("hisImage")
        }

        if(chatID==null)
            checkChat(hisID.toString())

        binding!!.image = hisImage
        binding!!.activity = this

        binding!!.btnSend.setOnClickListener {
            val message:String = binding!!.msgText.text.toString().trim()
            if(message.isNotEmpty()){
                sendMessage(message)
                val db = FirebaseDatabase.getInstance().getReference("Users").child(myID.toString())
                db.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        myName = snapshot.child("name").value.toString()
                        myImage = snapshot.child("image").value.toString()
                        getToken(message,hisID.toString(),myImage.toString(),chatID.toString(),myName.toString())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            }
            binding!!.msgText.setText("")
        }

        binding!!.msgText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.toString().trim().isEmpty()){
                    updateTypingStatus("false")
                }else{
                    updateTypingStatus(hisID.toString())
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        binding!!.attachFile.setOnClickListener {
            if(binding!!.dataLayout.visibility==View.INVISIBLE){
                showLayout()
            }else{
                hideLayout()
            }
        }

        //To hide the layout when user tap outside
        binding!!.msgText.setOnClickListener {
            hideLayout()
        }

        checkStatus(hisID.toString())

        binding!!.galleryImage.setOnClickListener {
            getGalleryImage()
        }




        val window = this.window
        window.statusBarColor = this.resources.getColor(R.color.primary,theme)


    }
    private fun checkChat(hisID:String){
        val databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myID.toString())
        val query = databaseReference.orderByChild("member").equalTo(hisID)
        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.hasChildren()){
                    for(ds in snapshot.children){
                        val id:String = ds.child("member").value.toString()
                        if(id == hisID){
                            chatID = ds.key
                            readMessages(chatID.toString())
                            break
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun createChat(msg:String){

        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myID.toString())
        chatID = databaseReference!!.push().key
        val chatListModel = ChatListModel(chatID,util.currentData(),msg,hisID)
        databaseReference!!.child(chatID.toString()).setValue(chatListModel)

        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(hisID.toString())
        val chatList = ChatListModel(chatID,util.currentData(),msg,myID)
        databaseReference!!.child(chatID.toString()).setValue(chatList)

        databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatID.toString())
        val messageModel = MessageModel(myID,hisID.toString(),msg,util.currentData().toString(),"text")
        databaseReference!!.push().setValue(messageModel)

        readMessages(chatID.toString())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun sendMessage(msg:String){
        binding?.messageRecyclerView?.smoothScrollToPosition(firebaseRecyclerAdapter!!.itemCount)
        if(chatID==null){
            createChat(msg)
        }else{
            val date = util.currentData()
            val messageModel = MessageModel(myID, hisID!!, msg, date!!, "text")
            databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatID!!)
            databaseReference!!.push().setValue(messageModel)

            val map: MutableMap<String, Any?> = HashMap()
            map["lastMessage"] = msg
            map["date"] = date
            databaseReference =
                FirebaseDatabase.getInstance().getReference("ChatList").child(myID!!).child(
                    chatID!!
                )
            databaseReference!!.updateChildren(map)

            databaseReference =
                FirebaseDatabase.getInstance().getReference("ChatList").child(hisID!!).child(
                    chatID!!
                )
            val update: MutableMap<String, Any> = HashMap()
            update["lastMessage"] = msg
            update["date"] = date
            databaseReference!!.updateChildren(update)

        }
    }

    fun userInfo(){
        val intent = Intent(this,UserInfo::class.java)
        intent.putExtra("userID",hisID)
        startActivity(intent)
    }

    private fun readMessages(chatID:String){
        val query = FirebaseDatabase.getInstance().reference.child("Chat").child(chatID)
        val options:FirebaseRecyclerOptions<MessageModel> = FirebaseRecyclerOptions.Builder<MessageModel>().setQuery(query,MessageModel::class.java).build()
        val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<MessageModel,ViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                return if(viewType==0){
                    val viewDataBinding:ViewDataBinding = RightItemLayoutBinding.inflate(
                        LayoutInflater.from(baseContext),parent,false
                    )

                    ViewHolder(viewDataBinding)
                }else{

                    val dataBinding:ViewDataBinding = LeftItemLayoutBinding.inflate(
                        LayoutInflater.from(baseContext),parent,false
                    )

                    ViewHolder(dataBinding)
                }

            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int, model: MessageModel) {

                holder.viewDataBinding.setVariable(BR.messageImage,hisImage)
                holder.viewDataBinding.setVariable(BR.message,model)


            }

            //This function is used to check whether it is send layout or receive layout
            override fun getItemViewType(position: Int): Int {

                val messageModel:MessageModel = getItem(position)
                return if(myID.equals(messageModel.getSender())){
                    0
                }else{

                    1
                }
            }

        }

        manager.stackFromEnd = true
        binding?.messageRecyclerView?.layoutManager = manager
        binding?.messageRecyclerView?.setHasFixedSize(false)
        binding?.messageRecyclerView?.smoothScrollToPosition(firebaseRecyclerAdapter!!.itemCount)
        binding?.messageRecyclerView?.itemAnimator = null
        binding?.messageRecyclerView?.adapter = firebaseRecyclerAdapter
        binding?.messageRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy < -5) {
                    binding!!.straightDown.visibility = View.VISIBLE
                }
                val layoutManager =  recyclerView.layoutManager as LinearLayoutManager?
                val visibleItemCount = layoutManager!!.childCount
                val totalItemCount = layoutManager.itemCount
                val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()


                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    binding!!.straightDown.visibility = View.GONE

                }
            }
        })

        // to know when the item count is increased
        binding?.messageRecyclerView?.adapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding?.messageRecyclerView?.smoothScrollToPosition(firebaseRecyclerAdapter!!.itemCount)
            }
        })

        //on clicking downwards arrow
        binding?.straightDown?.setOnClickListener {
            binding?.messageRecyclerView?.stopScroll()
            manager.scrollToPosition(firebaseRecyclerAdapter!!.itemCount-1)
        }

        (firebaseRecyclerAdapter as FirebaseRecyclerAdapter<MessageModel, ViewHolder>).startListening()

    }

    class ViewHolder(var viewDataBinding: ViewDataBinding) :
        RecyclerView.ViewHolder(
            viewDataBinding.root
        )

    private fun checkStatus(hisID: String){
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(hisID)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val online:String = snapshot.child("online").value.toString()
                    val typing:String = snapshot.child("typing").value.toString()
                    binding?.status = online
                    if(typing == myID){
                        binding!!.typingStatus.visibility = View.VISIBLE
                        binding!!.typingStatus.playAnimation()
                    }else{
                        binding!!.typingStatus.cancelAnimation()
                        binding!!.typingStatus.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun updateTypingStatus(status:String){
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myID.toString())
        val map:MutableMap<String,Any> = HashMap()
        map["typing"] = status
        databaseReference.updateChildren(map)
    }

    private fun getToken(message:String,hisID: String,myImage:String,chatID: String,myName:String){

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(hisID)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val token:String = snapshot.child("token").value.toString()

                val to = JSONObject()
                val data = JSONObject()
                try{
                    data.put("title",myName)
                    data.put("message",message)
                    data.put("hisID",myID)
                    data.put("hisImage",myImage)
                    data.put("chatID",chatID)

                    to.put("to",token)
                    to.put("data",data)

                    sendNotification(to)

                }catch(e:java.lang.Exception){
                    e.printStackTrace()
                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun sendNotification(to: JSONObject) {
        val request: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, AllConstants.NOTIFICATION_URL, to,
            Response.Listener { response: JSONObject ->
                Log.d(
                    "notification",
                    "sendNotification: $response"
                )
            },
            Response.ErrorListener { error: VolleyError ->
                Log.d(
                    "notification",
                    "sendNotification: $error"
                )
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val map: MutableMap<String, String> = HashMap()
                map["Authorization"] = "key=" + AllConstants.SERVER_KEY
                map["Content-Type"] = "application/json"
                return map
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        request.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(request)
    }

    private fun showLayout(){
        val view:RelativeLayout = binding!!.dataLayout
        val radius = view.width.coerceAtLeast(view.height)
        val animator = ViewAnimationUtils.createCircularReveal(view,view.left,view.top, 0F,
            radius.toFloat()*2
        )
        animator.duration = 600
        view.visibility = View.VISIBLE
        animator.start()
    }

    private fun hideLayout(){
        val view:RelativeLayout = binding!!.dataLayout
        val radius = view.width.coerceAtLeast(view.height)
        val animator = ViewAnimationUtils.createCircularReveal(view,view.left,view.top, radius.toFloat()*2,
            0F
        )
        animator.duration = 800
        animator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.INVISIBLE
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }

        })
        animator.start()
    }

    override fun onResume() {
        util.updateOnlineStatus("online")
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        util.updateOnlineStatus(System.currentTimeMillis().toString())
        util.hideKeyBoard(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if(binding!!.dataLayout.visibility==View.VISIBLE){
            hideLayout()
        }
        super.onBackPressed()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 300){
            selectedImages = data?.getStringArrayListExtra(Pix.IMAGE_RESULTS) as ArrayList<String>

            println(selectedImages)
            val intent = Intent(this,SendMediaService::class.java)
            intent.putExtra("hisID",hisID)
            intent.putExtra("chatID",chatID)
            intent.putStringArrayListExtra("media",selectedImages)

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                startForegroundService(intent);
            else startService(intent);
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    getGalleryImage()
                }else{
                    Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getGalleryImage(){
        val options = Options.init()
            .setRequestCode(300)
            .setCount(3)
            .setFrontfacing(false)
            .setExcludeVideos(true)
            .setVideoDurationLimitinSeconds(30)
            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)
            .setPath("/SpoutChat/Media")

        if(selectedImages!=null)
            options.preSelectedUrls = selectedImages

        Pix.start(this,options)
    }


}

