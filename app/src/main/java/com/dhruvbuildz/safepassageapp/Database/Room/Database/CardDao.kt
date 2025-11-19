package com.dhruvbuildz.safepassageapp.Database.Room.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Card

@Dao
interface CardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insetCard(card: Card)

    @Update
    suspend fun updateCard(card: Card)

    @Delete
    suspend fun deleteCard(card: Card)

    @Query("SELECT * FROM card_table WHERE userId = :userId")
    fun getCards(userId: String): LiveData<List<Card>>

    @Query("SELECT * FROM card_table WHERE carId = :cardId AND userId = :userId")
    fun getCardById(cardId: Int, userId: String): LiveData<Card>

}