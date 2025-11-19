package com.dhruvbuildz.safepassageapp.UI

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Document
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UserRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.DocumentViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UserViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.DocumentVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.UserVMFactory
import com.dhruvbuildz.safepassageapp.Fetures.DocumentManager
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.UI.Adapter.DocumentAdapter
import com.dhruvbuildz.safepassageapp.databinding.ActivityViewDocumentBinding

class ViewDocumentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewDocumentBinding
    private lateinit var documentViewModel: DocumentViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var documentAdapter: DocumentAdapter
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityViewDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViewModel()
        setupRecyclerView()
        setupViews()
        getCurrentUserAndObserveDocuments()
    }

    private fun initializeViewModel() {
        val database = SafePassageDatabase(this)
        val factory = DocumentVMFactory(database)
        documentViewModel = ViewModelProvider(this, factory)[DocumentViewModel::class.java]
        
        val userRepository = UserRepository(database)
        val userFactory = UserVMFactory(application, userRepository)
        userViewModel = ViewModelProvider(this, userFactory)[UserViewModel::class.java]
    }

    private fun setupRecyclerView() {
        documentAdapter = DocumentAdapter(
            onDocumentClick = { document -> openDocument(document) },
            onDocumentDelete = { document -> showDeleteConfirmation(document) } // ✅ use confirmation
        )

        binding.recyclerViewDocuments.apply {
            layoutManager = LinearLayoutManager(this@ViewDocumentActivity)
            adapter = documentAdapter
        }
    }

    private fun setupViews() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.fabAddDocument.setOnClickListener {
            val intent = Intent(this, AddDocumentActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getCurrentUserAndObserveDocuments() {
        userViewModel.getUser().observe(this, Observer { user ->
            user?.let {
                currentUserId = it.userId
                observeDocuments()
            }
        })
    }

    private fun observeDocuments() {
        currentUserId?.let { userId ->
            documentViewModel.getAllDocuments(userId).observe(this, Observer { documents ->
                if (documents.isEmpty()) {
                    binding.emptyStateLayout.visibility = View.VISIBLE
                    binding.recyclerViewDocuments.visibility = View.GONE
                } else {
                    binding.emptyStateLayout.visibility = View.GONE
                    binding.recyclerViewDocuments.visibility = View.VISIBLE
                    documentAdapter.submitList(documents)
                }
            })
        }
    }

    private fun openDocument(document: Document) {
        try {
            val file = java.io.File(document.filePath)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    file
                )

                var intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, document.mimeType)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(uri, "*/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    } else {
                        showFileInfoDialog(document)
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "${getString(R.string.error_opening_document)}: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showFileInfoDialog(document: Document) {
        val message = """
            File: ${document.fileName}
            Size: ${DocumentManager.formatFileSize(document.fileSize)}
            Type: ${document.mimeType}
            
            ${getString(R.string.no_app_found_message)}
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.file_information))
            .setMessage(message)
            .setPositiveButton(getString(R.string.share_file)) { _, _ ->
                shareFile(document)
            }
            .setNegativeButton("OK", null)
            .show()
    }

    private fun shareFile(document: Document) {
        try {
            val file = java.io.File(document.filePath)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND)
                intent.type = document.mimeType
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                startActivity(Intent.createChooser(intent, "Share File"))
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "${getString(R.string.error_sharing_file)}: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // 🔹 New method for delete confirmation
    private fun showDeleteConfirmation(document: Document) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_delete))
            .setMessage("Are you sure you want to delete '${document.title}'? This action cannot be undone.")
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                DocumentManager.deleteDocumentFile(document.filePath)
                documentViewModel.deleteDocument(document)
                Toast.makeText(
                    this,
                    getString(R.string.document_deleted_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
