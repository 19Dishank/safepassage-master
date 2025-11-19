package com.dhruvbuildz.safepassageapp.Database.Room.Repository

import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Category

class CategoryRepository(private val database: SafePassageDatabase) {

    suspend fun insertCategory(category: Category) = database.categoryDao().insertCategory(category)
    suspend fun updateCategory(category: Category) = database.categoryDao().updateCategory(category)
    suspend fun deleteCategory(category: Category) = database.categoryDao().deleteCategory(category)

    fun getAllCategories() = database.categoryDao().getAllCategories()

    fun getCategoryById(id: Int) = database.categoryDao().getCategoryById(id)
    fun getCategoryIdByName(name: String) = database.categoryDao().getCategoryIdByName(name)

}