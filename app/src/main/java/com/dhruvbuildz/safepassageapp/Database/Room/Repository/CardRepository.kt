package com.dhruvbuildz.safepassageapp.Database.Room.Repository

import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Card

class CardRepository(private val database: SafePassageDatabase) {

    suspend fun insertCard(card: Card) = database.cardDao().insetCard(card)
    suspend fun updateCard(card: Card) = database.cardDao().updateCard(card)
    suspend fun deleteCard(card: Card) = database.cardDao().deleteCard(card)

    fun getCards(userId: String) = database.cardDao().getCards(userId)
    fun getCardById(cardId: Int, userId: String) = database.cardDao().getCardById(cardId, userId)

}