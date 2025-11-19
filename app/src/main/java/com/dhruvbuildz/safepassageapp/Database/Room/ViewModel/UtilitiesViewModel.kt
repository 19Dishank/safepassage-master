package com.dhruvbuildz.safepassageapp.Database.Room.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Utilities
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UtilitiesRepository
import kotlinx.coroutines.launch

class UtilitiesViewModel(app:Application,private val utilitiesRepository: UtilitiesRepository) : AndroidViewModel(app) {

    fun insertUtilities(utilities: Utilities) = viewModelScope.launch {
        utilitiesRepository.insertUtilities(utilities)
    }

    fun updateUtilities(utilities: Utilities) = viewModelScope.launch {
        utilitiesRepository.updateUtilities(utilities)
    }

    fun deleteUtilities(utilities: Utilities) = viewModelScope.launch {
        utilitiesRepository.deleteUtilities(utilities)
    }

    fun getUtilities() = utilitiesRepository.getUtilities()

}