package com.dhruvbuildz.safepassageapp.Database.Room.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import com.dhruvbuildz.safepassageapp.Database.Room.Model.User
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UserRepository
import com.dhruvbuildz.safepassageapp.Utils.SessionManager
import kotlinx.coroutines.launch

class UserViewModel(app: Application, private val userRepository: UserRepository) :
    AndroidViewModel(app) {

    fun insertUser(user: User) = viewModelScope.launch {
        userRepository.insertUser(user)
    }

    fun updateUser(user: User) = viewModelScope.launch {
        userRepository.updateUser(user)
    }

    fun deleteUser(user: User) = viewModelScope.launch {
        userRepository.deleteUser(user)
    }

    fun getAnyUser() = userRepository.getAnyUser()

    fun getUserById(userId: String): LiveData<User?> = userRepository.getUserById(userId)

    fun getUser(): LiveData<User?> {
        val sessionManager = SessionManager(getApplication())
        val lastUserId = sessionManager.getLastUserId()
        return if (!lastUserId.isNullOrEmpty()) {
            userRepository.getUserById(lastUserId)
        } else {
            userRepository.getAnyUser()
        }
    }

}