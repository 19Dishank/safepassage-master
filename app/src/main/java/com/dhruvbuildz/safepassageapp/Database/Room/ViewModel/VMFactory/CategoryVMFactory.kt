package com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.CategoryRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.CategoryViewModel

class CategoryVMFactory(val app: Application, private val categoryRepository: CategoryRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoryViewModel(app, categoryRepository) as T
    }

}