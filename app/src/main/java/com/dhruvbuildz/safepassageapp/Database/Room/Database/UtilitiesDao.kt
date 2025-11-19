package com.dhruvbuildz.safepassageapp.Database.Room.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Utilities

@Dao
interface UtilitiesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUtilities(utilities: Utilities)

    @Update
    suspend fun updateUtilities(utilities: Utilities)

    @Delete
    suspend fun deleteUtilities(utilities: Utilities)

    @Query("SELECT * FROM Utilities LIMIT 1")
    fun getUtilities(): LiveData<Utilities>

}