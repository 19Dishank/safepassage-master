package com.dhruvbuildz.safepassageapp.Database.Room.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pin_table")
data class
Pin(
    @PrimaryKey
    val userId: String,
    val encryptedPin: String,
    val createdAt: Long = System.currentTimeMillis()
)
