package com.dhruvbuildz.safepassageapp.Database.Room.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Category

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM category_table")
    fun getAllCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM category_table WHERE catId = :id")
    fun getCategoryById(id: Int): LiveData<Category>

    @Query("SELECT * FROM category_table WHERE catName = :name LIMIT 1")
    fun getCategoryIdByName(name: String): LiveData<List<Category>>

}