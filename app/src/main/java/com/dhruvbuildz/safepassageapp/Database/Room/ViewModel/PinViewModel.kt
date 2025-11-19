package com.dhruvbuildz.safepassageapp.Database.Room.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Pin
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.PinRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.PinVMFactory
import kotlinx.coroutines.launch

class PinViewModel(application: Application, private val pinRepository: PinRepository) : AndroidViewModel(application) {

    fun insertPin(pin: Pin) = viewModelScope.launch {
        pinRepository.insertPin(pin)
    }

    fun updatePin(pin: Pin) = viewModelScope.launch {
        pinRepository.updatePin(pin)
    }

    fun deletePin(pin: Pin) = viewModelScope.launch {
        pinRepository.deletePin(pin)
    }

    fun getPinByUserId(userId: String): LiveData<Pin?> = pinRepository.getPinByUserId(userId)

    fun hasPin(userId: String): LiveData<Boolean> = pinRepository.hasPin(userId)

    companion object {
        fun create(application: Application): PinViewModel {
            val pinRepository = PinRepository(SafePassageDatabase(application).pinDao())
            return PinViewModel(application, pinRepository)
        }
    }
}
