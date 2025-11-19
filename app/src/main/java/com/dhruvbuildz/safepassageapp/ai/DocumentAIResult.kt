package com.dhruvbuildz.safepassageapp.ai

data class DocumentAIResult(
    val documentType: String?,
    val expiryDate: String?,
    val summary: String?,
    val confidence: Double?
)

