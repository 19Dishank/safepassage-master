package com.dhruvbuildz.safepassageapp.Database.Room.Model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "password_table",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["catId"],
            childColumns = ["catId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Parcelize
data class Password(
    @PrimaryKey(autoGenerate = true)
    val passId: Int,
    val userId: String,
    val title: String,
    val userName: String?,
    val email: String?,
    val phone: String?,
    val password: String?,
    val url: String?,
    val packageName: String?,
    val note: String?,
    val catId: Int?,
    val createdAt: String?,
    val updatedAt: String?,
    val lastUsed: String?
) : Parcelable
