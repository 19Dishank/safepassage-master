package com.dhruvbuildz.safepassageapp.Database.Room.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Password

@Dao
interface PasswordDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: Password)

    @Update
    suspend fun updatePassword(password: Password)

    @Delete
    suspend fun deletePassword(password: Password)

    @Query("SELECT * FROM password_table WHERE userId = :userId")
    fun getAllPasswords(userId: String): LiveData<List<Password>>

    @Query("SELECT * FROM password_table WHERE passId = :id AND userId = :userId")
    fun getPasswordById(id: Int, userId: String): LiveData<Password>

    @Query("SELECT * FROM password_table WHERE catId = :catId AND userId = :userId")
    fun getPasswordByCategory(catId: Int, userId: String): LiveData<List<Password>>

    @Query("SELECT * FROM password_table WHERE (title LIKE '%' || :searchText || '%' OR url LIKE '%' || :searchText || '%' OR note LIKE '%' || :searchText || '%') AND userId = :userId")
    fun searchPassword(searchText: String, userId: String): LiveData<List<Password>>

    // A to Z (case-insensitive)
    @Query("SELECT * FROM password_table WHERE userId = :userId ORDER BY title COLLATE NOCASE ASC")
    fun getPasswordsSortedByTitle(userId: String): LiveData<List<Password>>

    // Z to A (case-insensitive)
    @Query("SELECT * FROM password_table WHERE userId = :userId ORDER BY title COLLATE NOCASE DESC")
    fun getPasswordsSortedByTitleDesc(userId: String): LiveData<List<Password>>

    //    Newest to Oldest
    @Query("SELECT * FROM password_table WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPasswordsSortedByNewest(userId: String): LiveData<List<Password>>

    //    Newest to Oldest
    @Query("SELECT * FROM password_table WHERE userId = :userId ORDER BY createdAt ASC")
    fun getPasswordsSortedByOldest(userId: String): LiveData<List<Password>>

    //    Recently Used
    @Query("SELECT * FROM password_table WHERE userId = :userId ORDER BY lastUsed DESC")
    fun getPasswordsSortedByRecentlyUsed(userId: String): LiveData<List<Password>>


}