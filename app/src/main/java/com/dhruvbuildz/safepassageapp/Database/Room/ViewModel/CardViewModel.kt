package com.dhruvbuildz.safepassageapp.Database.Room.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Card
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.CardRepository
import kotlinx.coroutines.launch

class CardViewModel(app: Application, private val cardRepository: CardRepository) :
    AndroidViewModel(app) {

    fun insertCard(card: Card) = viewModelScope.launch {
        cardRepository.insertCard(card)
    }

    fun updateCard(card: Card) = viewModelScope.launch {
        cardRepository.updateCard(card)
    }

    fun deleteCard(card: Card) = viewModelScope.launch {
        cardRepository.deleteCard(card)
    }

    fun getCards(userId: String) = cardRepository.getCards(userId)
    fun getCardById(cardId: Int, userId: String) = cardRepository.getCardById(cardId, userId)

}