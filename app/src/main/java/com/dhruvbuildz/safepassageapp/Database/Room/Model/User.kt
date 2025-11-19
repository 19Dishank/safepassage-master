package com.dhruvbuildz.safepassageapp.Database.Room.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey
    val userId: String,
    val email: String,
    val userName: String,
    val createdAt: String,
)
