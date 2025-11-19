package com.dhruvbuildz.safepassageapp.Database.Room.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Pin

@Dao
interface PinDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPin(pin: Pin)

    @Update
    suspend fun updatePin(pin: Pin)

    @Delete
    suspend fun deletePin(pin: Pin)

    @Query("SELECT * FROM pin_table WHERE userId = :userId")
    fun getPinByUserId(userId: String): LiveData<Pin?>

    @Query("SELECT EXISTS(SELECT 1 FROM pin_table WHERE userId = :userId)")
    fun hasPin(userId: String): LiveData<Boolean>
}
