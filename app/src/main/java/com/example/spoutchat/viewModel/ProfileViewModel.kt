package com.example.spoutchat.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.spoutchat.UserModel
import com.example.spoutchat.repository.ProfileRepository


class ProfileViewModel : ViewModel() {
    private var profileRepository:ProfileRepository = ProfileRepository.getInstance()
    fun getUser(): LiveData<UserModel> {
        return profileRepository.getUser()
    }

    fun editImage(uri: String?) {
        profileRepository.editImage(uri.toString())
    }

    fun editStatus(status: String?) {
        profileRepository.editStatus(status.toString())
    }

    fun editUsername(name: String?) {
        profileRepository.editUsername(name.toString())
    }
}