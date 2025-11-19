package com.dhruvbuildz.safepassageapp.ai

import android.util.Log
import com.dhruvbuildz.safepassageapp.BuildConfig
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Document
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object DocumentAIAnalyzer {

    private const val TAG = "DocumentAIAnalyzer"
    private const val MODEL = "openai/gpt-4o-mini"
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun isConfigured(): Boolean {
        return BuildConfig.OPENROUTER_API_KEY.isNotBlank()
    }

    fun analyzeDocument(document: Document, extractedText: String): DocumentAIResult? {
        val apiKey = BuildConfig.OPENROUTER_API_KEY.trim()
        if (apiKey.isEmpty()) {
            Log.w(TAG, "OpenRouter API key not configured.")
            return null
        }

        if (extractedText.isBlank()) {
            return null
        }

        return try {
            val payload = buildPayload(document, extractedText)
            val body = payload.toString().toRequestBody(JSON_MEDIA)
            val request = Request.Builder()
                .url("${BuildConfig.OPENROUTER_BASE_URL}/chat/completions")
                .post(body)
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", BuildConfig.OPENROUTER_HTTP_REFERER)
                .header("X-Title", BuildConfig.OPENROUTER_APP_TITLE)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "OpenRouter response failure: ${response.code}")
                    return null
                }

                val responseBody = response.body?.string() ?: return null
                parseResponse(responseBody)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "AI analysis failed: ${ex.message}", ex)
            null
        }
    }

    private fun buildPayload(document: Document, extractedText: String): JSONObject {
        val systemMessage = JSONObject().apply {
            put("role", "system")
            put(
                "content",
                "You are an expert document analysis assistant. Your task is to thoroughly read and scan the ENTIRE document content provided to you. " +
                    "You must carefully analyze every section, field, and detail in the document to understand what it exactly is. " +
                    "\n\n" +
                    "IMPORTANT INSTRUCTIONS:\n" +
                    "1. Read and scan the COMPLETE document from start to finish\n" +
                    "2. Identify the EXACT document type and purpose (what is this document exactly?)\n" +
                    "3. Extract all relevant information including dates, names, numbers, and key details\n" +
                    "4. Provide a clear, concise one-line summary that captures the essence of the document\n" +
                    "5. Identify any expiry dates, expiration dates, valid until dates, or renewal dates\n" +
                    "\n\n" +
                    "You must respond ONLY with valid JSON in the following format:\n" +
                    "{\n" +
                    "  \"document_type\": \"exact_type_here\" (e.g. driver_license, passport, insurance_policy, id_card, visa, membership_card, certificate, diploma, transcript, question_bank, exam_paper, contract, invoice, receipt, medical_report, prescription, birth_certificate, marriage_certificate, tax_document, bank_statement, utility_bill, lease_agreement, employment_contract, resume, cv, academic_transcript, degree_certificate, training_certificate, license_certificate, permit, registration, warranty, manual, guide, form, application, letter, notice, report, other),\n" +
                    "  \"expiry_date\": \"YYYY-MM-DD\" or null (if no expiry date found, return null),\n" +
                    "  \"summary\": \"One clear sentence (max 80 words) that summarizes what this document is and its key purpose/information. Be specific and descriptive.\",\n" +
                    "  \"confidence\": 0.0-1.0 (your confidence level in the analysis)\n" +
                    "}\n\n" +
                    "CRITICAL: Never include markdown formatting, code blocks, or any text outside the JSON object. " +
                    "If you cannot determine an expiry date with certainty, return null for expiry_date. " +
                    "The summary must be a single, well-written sentence that clearly describes what the document is."
            )
        }

        val userPrompt = """
            Please analyze this document thoroughly. Read and scan the ENTIRE document content carefully to identify:
            1. What is this document exactly?
            2. What is its purpose and key information?
            3. Are there any expiry dates, expiration dates, or validity periods?
            4. Provide a clear one-line summary of the document.
            
            Document metadata:
            - Title: ${document.title}
            - File name: ${document.fileName}
            - MIME type: ${document.mimeType}
            
            FULL DOCUMENT CONTENT (read everything):
            \"\"\"$extractedText\"\"\"
            
            Now analyze the complete document and provide your response in the required JSON format.
        """.trimIndent()

        val userMessage = JSONObject().apply {
            put("role", "user")
            put("content", userPrompt)
        }

        val messages = JSONArray().apply {
            put(systemMessage)
            put(userMessage)
        }

        return JSONObject().apply {
            put("model", MODEL)
            put("messages", messages)
            put("temperature", 0.0)
            put("max_tokens", 1000) // Increased for more detailed analysis and summary
        }
    }

    private fun parseResponse(raw: String): DocumentAIResult? {
        return try {
            val root = JSONObject(raw)
            val choices = root.optJSONArray("choices") ?: return null
            if (choices.length() == 0) return null
            val content = choices.getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()

            val jsonContent = extractJson(content) ?: return null
            val result = JSONObject(jsonContent)

            val documentType = result.optString("document_type", "").ifBlank { null }
            val expiryDate = result.optString("expiry_date", "").ifBlank { null }
            val summary = result.optString("summary", "").ifBlank { null }
            val confidence = result.optDouble("confidence", Double.NaN)
            val confidenceValue = if (confidence.isNaN()) null else confidence

            DocumentAIResult(
                documentType = documentType,
                expiryDate = expiryDate,
                summary = summary,
                confidence = confidenceValue
            )
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to parse AI response: ${ex.message}", ex)
            null
        }
    }

    private fun extractJson(raw: String): String? {
        // Attempt to find first JSON object in the response
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        if (start == -1 || end == -1 || end <= start) {
            return null
        }
        return raw.substring(start, end + 1)
    }
}

