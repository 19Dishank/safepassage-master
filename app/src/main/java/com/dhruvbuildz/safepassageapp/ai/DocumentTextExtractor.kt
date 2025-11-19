package com.dhruvbuildz.safepassageapp.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Document
import com.dhruvbuildz.safepassageapp.Fetures.DocumentManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.Locale
import java.util.zip.ZipInputStream
import kotlin.text.Charsets

object DocumentTextExtractor {

    private const val TAG = "DocumentTextExtractor"

    private val SUPPORTED_TEXT_EXTENSIONS = setOf(
        "txt",
        "csv",
        "json",
        "md",
        "xml",
        "html",
        "htm",
        "log",
        "yaml",
        "yml"
    )

    private val SUPPORTED_IMAGE_EXTENSIONS = setOf(
        "jpg", "jpeg", "png", "webp", "bmp", "gif", "heic", "heif", "tiff"
    )

    @Volatile
    private var pdfBoxInitialised = false

    suspend fun extract(context: Context, document: Document, maxCharacters: Int = 16_000): String? =
        withContext(Dispatchers.IO) {
            val file = File(document.filePath)
            if (!file.exists() || !file.canRead()) {
                Log.w(TAG, "File not accessible: ${document.filePath}")
                return@withContext null
            }

            val mime = document.mimeType.lowercase(Locale.getDefault())
            val extension =
                DocumentManager.getFileExtension(document.fileName).lowercase(Locale.getDefault())

            when {
                mime.startsWith("text") || SUPPORTED_TEXT_EXTENSIONS.contains(extension) -> readTextFile(
                    file,
                    maxCharacters
                )

                isPdf(mime, extension) -> extractPdfText(context, file, maxCharacters)
                isImage(mime, extension) -> extractImageText(context, file, maxCharacters)
                isDocx(extension) -> extractDocxText(file, maxCharacters)
                else -> readTextFile(file, maxCharacters) ?: run {
                    Log.w(TAG, "Falling back to unsupported format handling for ${document.fileName}")
                    null
                }
            }
        }

    private fun isPdf(mime: String, ext: String): Boolean {
        return mime == "application/pdf" || ext == "pdf"
    }

    private fun isImage(mime: String, ext: String): Boolean {
        return mime.startsWith("image/") || SUPPORTED_IMAGE_EXTENSIONS.contains(ext)
    }

    private fun isDocx(ext: String): Boolean = ext == "docx"

    private fun readTextFile(file: File, maxCharacters: Int): String? {
        return try {
            val builder = StringBuilder()
            BufferedReader(InputStreamReader(file.inputStream(), Charset.defaultCharset())).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    builder.append(line).append('\n')
                    if (builder.length >= maxCharacters) {
                        break
                    }
                }
            }
            val text = builder.toString().trim()
            if (text.isEmpty()) null else text.take(maxCharacters)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read text file: ${e.message}")
            null
        }
    }

    private fun ensurePdfBoxInitialised(context: Context) {
        if (!pdfBoxInitialised) {
            synchronized(this) {
                if (!pdfBoxInitialised) {
                    PDFBoxResourceLoader.init(context.applicationContext)
                    pdfBoxInitialised = true
                }
            }
        }
    }

    private fun extractPdfText(context: Context, file: File, maxCharacters: Int): String? =
        try {
            ensurePdfBoxInitialised(context)
            val text = PDDocument.load(file).use { document ->
                if (document.isEncrypted) {
                    Log.w(TAG, "PDF is encrypted and cannot be processed: ${file.name}")
                    ""
                } else {
                    val stripper = PDFTextStripper().apply {
                        sortByPosition = true
                    }
                    stripper.getText(document)
                }
            }.trim()
            if (text.isEmpty()) null else text.take(maxCharacters)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract PDF text: ${e.message}", e)
            null
        }

    private suspend fun extractImageText(context: Context, file: File, maxCharacters: Int): String? =
        withContext(Dispatchers.Default) {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            try {
                val image = InputImage.fromFilePath(context, Uri.fromFile(file))
                val result = recognizer.process(image).await()
                val text = result.text?.trim() ?: ""
                if (text.isEmpty()) null else text.take(maxCharacters)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to run OCR on image: ${e.message}")
                null
            } finally {
                recognizer.close()
            }
        }

    private fun extractDocxText(file: File, maxCharacters: Int): String? {
        return try {
            ZipInputStream(FileInputStream(file)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml") {
                        val xml = zis.bufferedReader(Charsets.UTF_8).use { it.readText() }
                        val text = xml
                            .replace("<w:p[\\s\\S]*?>".toRegex(), "\n")
                            .replace("<[^>]+>".toRegex(), " ")
                            .replace("\\s+".toRegex(), " ")
                            .trim()
                        return if (text.isEmpty()) null else text.take(maxCharacters)
                    }
                    entry = zis.nextEntry
                }
            }
            null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract DOCX text: ${e.message}")
            null
        }
    }
}

