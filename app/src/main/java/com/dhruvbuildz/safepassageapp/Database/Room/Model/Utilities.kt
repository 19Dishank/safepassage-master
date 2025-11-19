package com.dhruvbuildz.safepassageapp.Database.Room.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Utilities")
data class Utilities(
    @PrimaryKey
    val uid : String = "1",
    val isLockApp : Boolean = false,
    val lockCode: String? = null,
    val isUsePhoneLock : Boolean = false,
    var isScreenshot : Boolean = false,
    var isDBSync : Boolean = false
)
