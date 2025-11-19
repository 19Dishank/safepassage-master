package com.dhruvbuildz.safepassageapp.Database.Room.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Password
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.PasswordRepository
import kotlinx.coroutines.launch

class PasswordViewModel(app: Application, private val passwordRepository: PasswordRepository) :
    AndroidViewModel(app) {

    fun insertPassword(password: Password) = viewModelScope.launch {
        passwordRepository.insertPassword(password)
    }

    fun updatePassword(password: Password) = viewModelScope.launch {
        passwordRepository.updatePassword(password)
    }

    fun deletePassword(password: Password) = viewModelScope.launch {
        passwordRepository.deletePassword(password)
    }

    fun getAllPasswords(userId: String) = passwordRepository.getAllPasswords(userId)

    fun getPasswordById(id: Int, userId: String) = passwordRepository.getPasswordById(id, userId)

    fun getPasswordByCategory(catId: Int, userId: String) = passwordRepository.getPasswordByCategory(catId, userId)

    fun searchPassword(searchText: String, userId: String) = passwordRepository.searchPassword(searchText, userId)

    fun getPasswordsSortedByTitle(userId: String) = passwordRepository.getPasswordsSortedByTitle(userId)
    fun getPasswordsSortedByTitleDesc(userId: String) = passwordRepository.getPasswordsSortedByTitleDesc(userId)
    fun getPasswordsSortedByNewest(userId: String) = passwordRepository.getPasswordsSortedByNewest(userId)
    fun getPasswordsSortedByOldest(userId: String) = passwordRepository.getPasswordsSortedByOldest(userId)
    fun getPasswordsSortedByRecentlyUsed(userId: String) = passwordRepository.getPasswordsSortedByRecentlyUsed(userId)

}