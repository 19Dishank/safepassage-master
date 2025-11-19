package com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.PasswordRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PasswordViewModel

class PasswordVMFactory(val app: Application, private val passwordRepository: PasswordRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PasswordViewModel(app, passwordRepository) as T
    }

}