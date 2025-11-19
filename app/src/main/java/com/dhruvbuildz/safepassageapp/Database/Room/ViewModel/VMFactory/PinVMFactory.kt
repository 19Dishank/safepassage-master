package com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.PinRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PinViewModel

class PinVMFactory(private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PinViewModel::class.java)) {
            val pinRepository = PinRepository(SafePassageDatabase(application).pinDao())
            @Suppress("UNCHECKED_CAST")
            return PinViewModel(application, pinRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
