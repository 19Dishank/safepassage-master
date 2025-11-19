package com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UtilitiesRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UtilitiesViewModel

class UtilitiesVMFactory(
    val app: Application,
    private val utilitiesRepository: UtilitiesRepository
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UtilitiesViewModel(app, utilitiesRepository) as T
    }
}