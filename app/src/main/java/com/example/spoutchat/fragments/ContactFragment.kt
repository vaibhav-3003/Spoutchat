package com.example.spoutchat.fragments

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.transition.TransitionManager
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spoutchat.R
import com.example.spoutchat.UserModel
import com.example.spoutchat.adapter.ContactAdapter
import com.example.spoutchat.databinding.FragmentContactBinding
import com.example.spoutchat.permissions.Permissions
import com.example.spoutchat.utils.Util
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContactFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactFragment : Fragment() {

    private var binding: FragmentContactBinding? = null
    private lateinit var databaseReference: DatabaseReference
    private val permissions = Permissions()
    private var userContacts:ArrayList<UserModel>? = ArrayList()
    private var appContacts:ArrayList<UserModel>? = ArrayList()
    private var userPhoneNumber: String? = null
    private var contactAdapter:ContactAdapter? = null
    private val util = Util()


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
        binding = FragmentContactBinding.inflate(inflater, container, false)
        contactAdapter = ContactAdapter(context,appContacts)

        // ToolBar as action bar
        (this.activity as AppCompatActivity?)?.setSupportActionBar(binding!!.contactToolBar)
        binding!!.contactToolBar.collapseIcon = ContextCompat.getDrawable(requireContext(), R.drawable.round_arrow_back_24);
        setHasOptionsMenu(true)

        binding!!.contactRecyclerView.layoutManager = LinearLayoutManager(context)
        binding!!.contactRecyclerView.setHasFixedSize(true)
        val firebaseAuth = FirebaseAuth.getInstance()
        userPhoneNumber = firebaseAuth.currentUser!!.phoneNumber
        println(userPhoneNumber)

        getUserContacts()


        return binding!!.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ContactFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ContactFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)


        inflater.inflate(R.menu.menu_search,menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem?.actionView as SearchView
        searchView.queryHint = "Search..."
        searchView.setIconifiedByDefault(false)
        val searchPlate = searchView.findViewById<View>(androidx.appcompat.R.id.search_plate)
        searchPlate.setBackgroundColor(Color.TRANSPARENT)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {

                contactAdapter?.filter?.filter(newText)

                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                return false
            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                TransitionManager.beginDelayedTransition(requireActivity().findViewById<View>(R.id.contactToolBar) as ViewGroup)
                MenuItemCompat.expandActionView(item)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Function for fetching contacts which uses this app
    private fun getUserContacts(){
        if (permissions.isContactOk(context)){
            val projection = arrayOf(
                ContactsContract.Contacts.DISPLAY_NAME,
                Phone.NUMBER
            )
            val cr:ContentResolver = requireActivity().contentResolver
            val cursor = cr.query(Phone.CONTENT_URI,projection,null,null,null)


            if(cursor!=null){
                userContacts?.clear()
                while(cursor.moveToNext()){
                    val name:String = cursor.getString(cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME))
                    var number:String = cursor.getString(cursor.getColumnIndexOrThrow(Phone.NUMBER))

                    number = number.replace("\\s","")
                    val num: String = java.lang.String.valueOf(number[0])

                    if(num == "0")
                        number = number.replace("(?:0)+","+91")

                    val userModel = UserModel()
                    userModel.setName(name)
                    userModel.setNumber(number)
                    userContacts?.add(userModel)

                }
            }
            cursor?.close()
            getAppContacts(userContacts)

        }else{
            permissions.requestContact(activity)
        }
    }

    private fun getAppContacts(userContacts: ArrayList<UserModel>?) {
        if (userContacts!=null){

            databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            val query = databaseReference.orderByChild("number")
            query.addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        appContacts?.clear()
                        for (ds in snapshot.children) {
                            val number:String = ds.child("number").value.toString()
                            val countryCode:String = ds.child("countryCode").value.toString()
                            val nwcc:String = countryCode+number

                            for(userModel in userContacts){
                                if((userModel.getNumber().equals(number) || userModel.getNumber().equals(nwcc)) && nwcc!=userPhoneNumber){
                                    val status:String = ds.child("status").value.toString()
                                    val uID = ds.child("uID").value.toString()
                                    val image = ds.child("image").value.toString()
                                    val name = ds.child("name").value.toString()

                                    val um = UserModel()
                                    um.setName(name)
                                    um.setStatus(status)
                                    um.setuID(uID)
                                    um.setImage(image)
                                    appContacts?.add(um)
                                    break
                                }
                            }
                        }
                        binding!!.contactRecyclerView.adapter = contactAdapter

                    }else{
                        Toast.makeText(context,"No Data Found",Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context,"cancelled",Toast.LENGTH_SHORT).show()
                }
            })

        }
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               getUserContacts()
            } else {
                Toast.makeText(context, "Permission denied!", Toast.LENGTH_SHORT).show()
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