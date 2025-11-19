package com.dhruvbuildz.safepassageapp.Database.Room.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_table")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val catId: Int,
    val catName: String,
    val createdAt: String
)
