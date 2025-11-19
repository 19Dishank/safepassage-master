package com.dhruvbuildz.safepassageapp.UI

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Document
import com.dhruvbuildz.safepassageapp.Database.Room.Model.DocumentAIStatus
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UserRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.DocumentViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UserViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.DocumentVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.UserVMFactory
import com.dhruvbuildz.safepassageapp.Fetures.DocumentManager
import com.dhruvbuildz.safepassageapp.Fetures.Utils
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.ai.DocumentAIOrchestrator
import com.dhruvbuildz.safepassageapp.databinding.ActivityAddDocumentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddDocumentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDocumentBinding
    private lateinit var documentViewModel: DocumentViewModel
    private lateinit var userViewModel: UserViewModel
    private var selectedFileUri: Uri? = null
    private var selectedFileName: String = ""
    private var selectedFileSize: Long = 0L
    private var selectedMimeType: String = ""
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViewModel()
        getCurrentUser()
        setUpViews()
    }



    private fun initializeViewModel() {
        val database = SafePassageDatabase(this)
        val factory = DocumentVMFactory(database)
        documentViewModel = ViewModelProvider(this, factory)[DocumentViewModel::class.java]
        
        val userRepository = UserRepository(database)
        val userFactory = UserVMFactory(application, userRepository)
        userViewModel = ViewModelProvider(this, userFactory)[UserViewModel::class.java]
    }

    private fun getCurrentUser() {
        userViewModel.getUser().observe(this, Observer { user ->
            user?.let {
                currentUserId = it.userId
            }
        })
    }

    private fun setUpViews() {
        binding.buttonUpload.setOnClickListener {
            Utils.pickDocument(this)
        }

        binding.buttonUnlock.setOnClickListener {
            saveDocument()
        }

        // Add text change listener to enable/disable upload button
        binding.titleTextBox.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateUploadButtonState()
            }
        })
    }

    private fun updateUploadButtonState() {
        val hasTitle = binding.titleTextBox.text.toString().trim().isNotEmpty()
        val hasFile = selectedFileUri != null
        
        binding.buttonUnlock.isEnabled = hasTitle && hasFile
        
        // Clear error messages when user types
        if (hasTitle) {
            binding.titleErrorText.visibility = View.GONE
        }
        if (hasFile) {
            binding.uploadErrorText.visibility = View.GONE
        }
    }

    // Function to extract file name from URI
    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }

        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }

        return result ?: "Unknown"
    }


    private fun saveDocument() {
        val title = binding.titleTextBox.text.toString().trim()
        
        if (title.isEmpty()) {
            binding.titleErrorText.visibility = View.VISIBLE
            return
        }
        
        if (selectedFileUri == null) {
            binding.uploadErrorText.visibility = View.VISIBLE
            return
        }
        
        if (currentUserId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.titleErrorText.visibility = View.GONE
        binding.uploadErrorText.visibility = View.GONE
        
        // Show loading state
        binding.buttonUnlock.isEnabled = false
        binding.buttonUnlock.text = getString(R.string.uploading)
        
        try {
            val savedFilePath = DocumentManager.copyFileToInternalStorage(
                this,
                selectedFileUri!!,
                selectedFileName
            )

            if (savedFilePath == null) {
                Toast.makeText(this, getString(R.string.failed_to_save_document), Toast.LENGTH_SHORT).show()
                binding.buttonUnlock.isEnabled = true
                binding.buttonUnlock.text = getString(R.string.upload_document)
                return
            }

            val document = Document(
                userId = currentUserId!!,
                title = title,
                fileName = selectedFileName,
                filePath = savedFilePath,
                fileSize = selectedFileSize,
                mimeType = selectedMimeType,
                createdAt = DocumentManager.getCurrentTimestamp(),
                updatedAt = DocumentManager.getCurrentTimestamp(),
                aiStatus = DocumentAIStatus.PENDING
            )

            lifecycleScope.launch {
                try {
                    val newId = documentViewModel.insertDocument(document).toInt()
                    val savedDocument = document.copy(documentId = newId)

                    withContext(Dispatchers.Main) {
                        android.util.Log.d("AddDocumentActivity", "Document inserted: ${document.title} for user: ${document.userId}")
                        Toast.makeText(
                            this@AddDocumentActivity,
                            getString(R.string.document_uploaded_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                        setResult(RESULT_OK)
                        finish()
                    }

                    DocumentAIOrchestrator.scheduleAnalysis(applicationContext, savedDocument)
                } catch (ex: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddDocumentActivity, "Error: ${ex.message}", Toast.LENGTH_SHORT).show()
                        binding.buttonUnlock.isEnabled = true
                        binding.buttonUnlock.text = getString(R.string.upload_document)
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.buttonUnlock.isEnabled = true
            binding.buttonUnlock.text = getString(R.string.upload_document)
        }
    }

    // Handle the result of file picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Utils.REQUEST_CODE_PICK_DOCUMENT && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                selectedFileUri = uri
                selectedFileName = getFileNameFromUri(this, uri)
                selectedFileSize = DocumentManager.getFileSize(this, uri)
                selectedMimeType = DocumentManager.getMimeType(this, uri)
                
                // Update UI to show selected file info
                binding.selectedFileName.text = selectedFileName
                binding.selectedFileSize.text = DocumentManager.formatFileSize(selectedFileSize)
                binding.fileInfoContainer.visibility = View.VISIBLE
                
                // Update upload button state
                updateUploadButtonState()
            }
        }
    }
}