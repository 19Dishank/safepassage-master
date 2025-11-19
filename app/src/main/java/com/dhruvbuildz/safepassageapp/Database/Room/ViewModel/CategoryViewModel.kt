package com.dhruvbuildz.safepassageapp.Database.Room.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Category
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel(app: Application, private val categoryRepository: CategoryRepository) :
    AndroidViewModel(app) {

    fun insertCategory(category: Category) = viewModelScope.launch {
        categoryRepository.insertCategory(category)
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        categoryRepository.updateCategory(category)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        categoryRepository.deleteCategory(category)
    }

    fun getAllCategory() = categoryRepository.getAllCategories()

    fun getCategoryById(id: Int) = categoryRepository.getCategoryById(id)
    fun getCategoryIdByName(name: String) = categoryRepository.getCategoryIdByName(name)

}