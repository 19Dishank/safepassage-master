package com.dhruvbuildz.safepassageapp.ai

import android.content.Context
import android.util.Log
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Document
import com.dhruvbuildz.safepassageapp.Database.Room.Model.DocumentAIStatus
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.DocumentRepository
import com.dhruvbuildz.safepassageapp.Fetures.DocumentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object DocumentAIOrchestrator {

    private const val TAG = "DocumentAIOrchestrator"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun scheduleAnalysis(context: Context, document: Document) {
        scope.launch {
            val database = SafePassageDatabase(context)
            val repository = DocumentRepository(database)

            if (!DocumentAIAnalyzer.isConfigured()) {
                val failedDoc = document.copy(
                    aiStatus = DocumentAIStatus.FAILED,
                    aiFailureReason = "AI API key missing. Configure OPENROUTER_API_KEY.",
                    aiLastAnalyzedAt = DocumentManager.getCurrentTimestamp()
                )
                repository.updateDocument(failedDoc)
                return@launch
            }

            try {
                val extracted = DocumentTextExtractor.extract(context, document)
                if (extracted.isNullOrBlank()) {
                    val failureReason = buildNoTextReason(document)
                    val unsupportedDoc = document.copy(
                        aiStatus = DocumentAIStatus.UNSUPPORTED,
                        aiFailureReason = failureReason,
                        aiLastAnalyzedAt = DocumentManager.getCurrentTimestamp()
                    )
                    repository.updateDocument(unsupportedDoc)
                    return@launch
                }

                val aiResult = withContext(Dispatchers.IO) {
                    DocumentAIAnalyzer.analyzeDocument(document, extracted)
                }

                if (aiResult == null) {
                    val failedDoc = document.copy(
                        aiStatus = DocumentAIStatus.FAILED,
                        aiFailureReason = "AI could not interpret this document.",
                        aiLastAnalyzedAt = DocumentManager.getCurrentTimestamp()
                    )
                    repository.updateDocument(failedDoc)
                    return@launch
                }

                val normalizedExpiry = normalizeDate(aiResult.expiryDate)

                val updatedDocument = document.copy(
                    aiDocumentType = aiResult.documentType,
                    aiExpiryDate = normalizedExpiry,
                    aiSummary = aiResult.summary,
                    aiConfidence = aiResult.confidence,
                    aiLastAnalyzedAt = DocumentManager.getCurrentTimestamp(),
                    aiStatus = DocumentAIStatus.SUCCESS,
                    aiFailureReason = null
                )

                repository.updateDocument(updatedDocument)
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to analyze document ${document.documentId}: ${ex.message}", ex)
                val failedDoc = document.copy(
                    aiStatus = DocumentAIStatus.FAILED,
                    aiFailureReason = ex.localizedMessage ?: "Unknown error",
                    aiLastAnalyzedAt = DocumentManager.getCurrentTimestamp()
                )
                repository.updateDocument(failedDoc)
            }
        }
    }

    private val EXPECTED_FORMATS = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US),
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US),
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US),
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US),
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)
    )

    private fun normalizeDate(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        for (formatter in EXPECTED_FORMATS) {
            try {
                val parsed = LocalDate.parse(raw.trim(), formatter)
                return parsed.format(DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (_: DateTimeParseException) {
                continue
            }
        }
        return null
    }

    private fun buildNoTextReason(document: Document): String {
        val mime = document.mimeType.lowercase(Locale.getDefault())
        val extension = DocumentManager
            .getFileExtension(document.fileName)
            .lowercase(Locale.getDefault())

        return when {
            mime.startsWith("image/") || extension in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp") ->
                "AI could not detect readable text in this image. Try uploading a clearer scan."

            extension == "pdf" ->
                "This PDF doesn't contain selectable text (likely a scan). Please upload a clearer version or enable OCR."

            extension == "docx" ->
                "AI could not extract text from this DOCX file. Ensure the document is not password protected."

            else -> "AI could not find readable text in this document format."
        }
    }
}
