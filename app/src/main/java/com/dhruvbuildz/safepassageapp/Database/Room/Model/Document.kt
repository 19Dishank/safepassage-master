package com.dhruvbuildz.safepassageapp.Database.Room.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "document_table")
data class Document(
    @PrimaryKey(autoGenerate = true)
    val documentId: Int = 0,
    val userId: String,
    val title: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val mimeType: String,
    val createdAt: String,
    val updatedAt: String,
    val aiDocumentType: String? = null,
    val aiExpiryDate: String? = null,
    val aiSummary: String? = null,
    val aiConfidence: Double? = null,
    val aiLastAnalyzedAt: String? = null,
    val aiStatus: String = DocumentAIStatus.NOT_REQUESTED,
    val aiFailureReason: String? = null
)
