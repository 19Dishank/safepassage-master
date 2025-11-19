package com.dhruvbuildz.safepassageapp.Database.Room.Model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("card_table")
data class Card(
    @PrimaryKey(autoGenerate = true)
    val carId: Int = 0,

    val title: String,
    val userId: String,
    val cardHolderName: String?,
    val cardNumber: String?,
    val expirationDate: String?,
    val cvv: String?,
    val pin: String?,
    val note: String?

)
