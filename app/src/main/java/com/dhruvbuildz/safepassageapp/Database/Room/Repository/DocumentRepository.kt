package com.dhruvbuildz.safepassageapp.Database.Room.Repository

import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Document

class DocumentRepository(private val database: SafePassageDatabase) {

    suspend fun insertDocument(document: Document) = database.documentDao().insertDocument(document)
    suspend fun updateDocument(document: Document) = database.documentDao().updateDocument(document)
    suspend fun deleteDocument(document: Document) = database.documentDao().deleteDocument(document)

    fun getAllDocuments(userId: String) = database.documentDao().getAllDocuments(userId)
    fun getDocumentById(id: Int, userId: String) = database.documentDao().getDocumentById(id, userId)
    fun getDocumentsByUserId(userId: String) = database.documentDao().getDocumentsByUserId(userId)
    fun searchDocuments(searchText: String, userId: String) = database.documentDao().searchDocuments(searchText, userId)
    fun getDocumentsSortedByTitle(userId: String) = database.documentDao().getDocumentsSortedByTitle(userId)
    fun getDocumentsSortedByNewest(userId: String) = database.documentDao().getDocumentsSortedByNewest(userId)
    fun getDocumentsSortedByOldest(userId: String) = database.documentDao().getDocumentsSortedByOldest(userId)
    fun getDocumentsWithExpiry(userId: String) = database.documentDao().getDocumentsWithExpiry(userId)
}
