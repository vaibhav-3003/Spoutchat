package com.example.spoutchat.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.spoutchat.R
import com.example.spoutchat.UserModel
import com.example.spoutchat.activities.Message
import com.example.spoutchat.activities.UserInfo
import com.example.spoutchat.databinding.ContactItemLayoutBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.annotations.NotNull
import java.util.*


class ContactAdapter(private var context: Context?, private var arrayList: ArrayList<UserModel>?) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>() ,Filterable{

    private val databaseReference:DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
    private var filterArrayList:ArrayList<UserModel> = ArrayList()



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        filterArrayList.clear()
        filterArrayList.addAll(arrayList!!)
        databaseReference.keepSynced(true)
        val binding: ContactItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.contact_item_layout, parent, false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userModel:UserModel = arrayList!![position]
        holder.layoutBinding.userModel = userModel


        // for showing the user info activity
        holder.layoutBinding.showContactImgInfo.setOnClickListener {
            val intent = Intent(context,UserInfo::class.java)
            intent.putExtra("userID",userModel.getuID())
            context?.startActivity(intent)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context,Message::class.java)
            intent.putExtra("hisID",userModel.getuID())
            intent.putExtra("hisImage",userModel.getImage())
            context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return arrayList!!.size
    }


    class ViewHolder(@param:NotNull var layoutBinding: ContactItemLayoutBinding) :
        RecyclerView.ViewHolder(layoutBinding.root)

    override fun getFilter(): Filter {
        return contactFilter
    }
    private val contactFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<UserModel> = ArrayList()
            if (constraint.isEmpty())
                filteredList.addAll(filterArrayList)
            else {
                val filter = constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                for (userModel in filterArrayList) {
                    if (userModel.getName()!!.lowercase(Locale.getDefault())
                            .contains(filter)
                    ) filteredList.add(userModel)
                }
            }
            val filterResults = FilterResults()
            filterResults.values = filteredList
            return filterResults
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            arrayList!!.clear()
            arrayList!!.addAll(results.values as Collection<UserModel>)
            notifyDataSetChanged()
        }
    }

}
