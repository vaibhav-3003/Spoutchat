package com.example.spoutchat.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.transition.TransitionManager
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spoutchat.ChatListModel
import com.example.spoutchat.ChatModel
import com.example.spoutchat.R
import com.example.spoutchat.activities.Message
import com.example.spoutchat.databinding.ChatItemLayoutBinding
import com.example.spoutchat.databinding.FragmentChatBinding
import com.example.spoutchat.utils.Util
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChatFragment : Fragment() {

    private var binding: FragmentChatBinding? = null
    private val util = Util()
    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<ChatListModel,ViewHolder>? = null

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false)

        // ToolBar as action bar
        (this.activity as AppCompatActivity?)?.setSupportActionBar(binding!!.chatToolBar)
        binding!!.chatToolBar.collapseIcon = ContextCompat.getDrawable(requireContext(), R.drawable.round_arrow_back_24);
        setHasOptionsMenu(true)

        readChat()

        return binding!!.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun readChat(){

        val query = FirebaseDatabase.getInstance().getReference("ChatList").child(util.getUID().toString())
        val options:FirebaseRecyclerOptions<ChatListModel> = FirebaseRecyclerOptions.Builder<ChatListModel>().setQuery(query,ChatListModel::class.java).build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<ChatListModel,ViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

                val binding:ChatItemLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()),
                    R.layout.chat_item_layout,parent,false)
                return ViewHolder(binding)

            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ChatListModel) {

                val userID:String = model.getMember().toString()
                val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userID)
                databaseReference.addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            var time: Date? = null
                            val name:String = snapshot.child("name").value.toString()
                            val image:String = snapshot.child("image").value.toString()
                            val online:String = snapshot.child("online").value.toString()
                            val calendar:Calendar = Calendar.getInstance()
                            try{
                                time = util.sdf().parse(model.getDate().toString())
                            }catch (e:java.lang.Exception){
                                e.printStackTrace()
                            }
                            if (time != null) {
                                calendar.time = time
                            }
                            val date: String? = util.getTimeAgo(calendar.timeInMillis)

                            // to get the last child of the path
                            val rootRef = FirebaseDatabase.getInstance().reference
                            val itemsRef = rootRef.child("Chat").child(model.getChatListID().toString())

                            itemsRef.orderByKey().limitToLast(1)
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        for (childSnapshot in dataSnapshot.children) {
                                            val lastChildKey = childSnapshot.key
                                            val databaseReference2 = FirebaseDatabase.getInstance().getReference("Chat").child(model.getChatListID().toString()).child(lastChildKey.toString())
                                            databaseReference2.addListenerForSingleValueEvent(object : ValueEventListener{
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    if(snapshot.exists()){
                                                        val type = snapshot.child("type").value.toString()
                                                        if(type=="text"){
                                                            holder.binding.photoImage.visibility = View.GONE
                                                            val chatModel = ChatModel(model.getChatListID().toString(),name,model.getLastMessage().toString(),image,date.toString(),online)
                                                            holder.binding.chatModel = chatModel
                                                        }else if(type=="image"){
                                                            holder.binding.photoImage.visibility = View.VISIBLE
                                                            val chatModel = ChatModel(model.getChatListID().toString(),name,"Photo",image,date.toString(),online)
                                                            holder.binding.chatModel = chatModel
                                                        }
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    TODO("Not yet implemented")
                                                }

                                            })
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        // Handle error
                                    }
                                })




                            holder.itemView.setOnClickListener{
                                val intent = Intent(context,Message::class.java)
                                intent.putExtra("hisID",userID)
                                intent.putExtra("hisImage",image)
                                intent.putExtra("chatID",model.getChatListID())
                                startActivity(intent)
                            }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

            }
        }

        binding?.chatRecyclerView?.layoutManager = LinearLayoutManager(context)
        binding?.chatRecyclerView?.setHasFixedSize(false)
        binding?.chatRecyclerView?.adapter = firebaseRecyclerAdapter

    }


    class ViewHolder(binding: ChatItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var binding: ChatItemLayoutBinding

        init {
            this.binding = binding
        }
    }

    override fun onResume() {
        util.updateOnlineStatus("online")
        firebaseRecyclerAdapter?.startListening()
        super.onResume()
    }

    override fun onPause() {
        util.updateOnlineStatus(System.currentTimeMillis().toString())
        super.onPause()
    }

    override fun onStop() {
        firebaseRecyclerAdapter?.stopListening()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem?.actionView as SearchView
        searchView.queryHint = "Search..."
        searchView.setIconifiedByDefault(false)
        val searchPlate = searchView.findViewById<View>(androidx.appcompat.R.id.search_plate)
        searchPlate.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                TransitionManager.beginDelayedTransition(requireActivity().findViewById<View>(R.id.chatToolBar) as ViewGroup)
                MenuItemCompat.expandActionView(item)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}