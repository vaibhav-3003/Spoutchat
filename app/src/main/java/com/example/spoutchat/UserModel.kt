package com.example.spoutchat

import android.widget.ImageView
import com.bumptech.glide.Glide
import androidx.databinding.BindingAdapter
import com.ortiz.touchview.TouchImageView
import de.hdodenhof.circleimageview.CircleImageView


class UserModel {
    private var name: String? = null
    private var status:String? = null
    private var image:String? = null
    private var number:String? = null
    private var code:String? =null
    private var uID:String? = null
    private var online:String? = null
    private var typing:String? = null
    private var token:String? = null

    constructor(){}

    constructor(
        name: String?,
        status: String?,
        image: String?,
        number: String?,
        code:String?,
        uID: String?,
        online: String?,
        typing: String?,
        token: String?
    ) {
        this.name = name
        this.status = status
        this.image = image
        this.code = code
        this.number = number
        this.uID = uID
        this.online = online
        this.typing = typing
        this.token = token
    }

    fun getToken(): String? {
        return token
    }

    fun setToken(token: String?) {
        this.token = token
    }

    fun getTyping(): String? {
        return typing
    }

    fun setTyping(typing: String?) {
        this.typing = typing
    }

    fun getOnline(): String? {
        return online
    }

    fun setOnline(online: String?) {
        this.online = online
    }

    fun getName(): String? {
        return name
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String?) {
        this.status = status
    }

    fun getImage(): String? {
        return image
    }

    fun setImage(image: String?) {
        this.image = image
    }

    fun getNumber(): String? {
        return number
    }

    fun setNumber(number: String?) {
        this.number = number
    }
    fun getCountryCode(): String? {
        return code
    }

    fun setCountryCode(code: String?) {
        this.code = code
    }

    fun getuID(): String? {
        return uID
    }

    fun setuID(uID: String?) {
        this.uID = uID
    }



    companion object{

        @JvmStatic
        @BindingAdapter("imageChat")
        fun loadImage(view: ImageView, image: String?) {
            Glide.with(view.context).load(image).into(view)
        }

        @JvmStatic
        @BindingAdapter("touchImage")
        fun loadImage(view: TouchImageView, image: String?) {
            Glide.with(view.context).load(image).into(view)
        }

        @JvmStatic
        @BindingAdapter("imageUrl")
        fun loadImage(view: CircleImageView, image: String?) {
            Glide.with(view.context).load(image).into(view)
        }
    }
}

