package com.dhruvbuildz.safepassageapp.Database.Room.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Document
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.DocumentRepository
import kotlinx.coroutines.launch

class DocumentViewModel(private val repository: DocumentRepository) : ViewModel() {

    suspend fun insertDocument(document: Document): Long {
        return repository.insertDocument(document)
    }

    fun updateDocument(document: Document) = viewModelScope.launch {
        repository.updateDocument(document)
    }

    fun deleteDocument(document: Document) = viewModelScope.launch {
        repository.deleteDocument(document)
    }

    fun getAllDocuments(userId: String): LiveData<List<Document>> = repository.getAllDocuments(userId)
    fun getDocumentById(id: Int, userId: String): LiveData<Document> = repository.getDocumentById(id, userId)
    fun getDocumentsByUserId(userId: String): LiveData<List<Document>> = repository.getDocumentsByUserId(userId)
    fun searchDocuments(searchText: String, userId: String): LiveData<List<Document>> = repository.searchDocuments(searchText, userId)
    fun getDocumentsSortedByTitle(userId: String): LiveData<List<Document>> = repository.getDocumentsSortedByTitle(userId)
    fun getDocumentsSortedByNewest(userId: String): LiveData<List<Document>> = repository.getDocumentsSortedByNewest(userId)
    fun getDocumentsSortedByOldest(userId: String): LiveData<List<Document>> = repository.getDocumentsSortedByOldest(userId)
    fun getDocumentsWithExpiry(userId: String): LiveData<List<Document>> = repository.getDocumentsWithExpiry(userId)
}
