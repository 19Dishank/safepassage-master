package com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.DocumentRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.DocumentViewModel

class DocumentVMFactory(private val database: SafePassageDatabase) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocumentViewModel::class.java)) {
            val repository = DocumentRepository(database)
            @Suppress("UNCHECKED_CAST")
            return DocumentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
