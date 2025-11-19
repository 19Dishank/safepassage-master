package com.dhruvbuildz.safepassageapp.Database.Room.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Document

@Dao
interface DocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long

    @Update
    suspend fun updateDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("SELECT * FROM document_table WHERE userId = :userId")
    fun getAllDocuments(userId: String): LiveData<List<Document>>

    @Query("SELECT * FROM document_table WHERE documentId = :id AND userId = :userId")
    fun getDocumentById(id: Int, userId: String): LiveData<Document>

    @Query("SELECT * FROM document_table WHERE userId = :userId")
    fun getDocumentsByUserId(userId: String): LiveData<List<Document>>

    @Query("SELECT * FROM document_table WHERE (title LIKE '%' || :searchText || '%' OR fileName LIKE '%' || :searchText || '%') AND userId = :userId")
    fun searchDocuments(searchText: String, userId: String): LiveData<List<Document>>

    @Query("SELECT * FROM document_table WHERE userId = :userId ORDER BY title COLLATE NOCASE ASC")
    fun getDocumentsSortedByTitle(userId: String): LiveData<List<Document>>

    @Query("SELECT * FROM document_table WHERE userId = :userId ORDER BY createdAt DESC")
    fun getDocumentsSortedByNewest(userId: String): LiveData<List<Document>>

    @Query("SELECT * FROM document_table WHERE userId = :userId ORDER BY createdAt ASC")
    fun getDocumentsSortedByOldest(userId: String): LiveData<List<Document>>

    @Query("SELECT * FROM document_table WHERE userId = :userId AND aiExpiryDate IS NOT NULL ORDER BY aiExpiryDate ASC")
    fun getDocumentsWithExpiry(userId: String): LiveData<List<Document>>
}
