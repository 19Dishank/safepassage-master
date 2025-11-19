package com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.CardRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.CardViewModel

class CardVMFactory(val app: Application, private val cardRepository: CardRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CardViewModel(app, cardRepository) as T
    }

}