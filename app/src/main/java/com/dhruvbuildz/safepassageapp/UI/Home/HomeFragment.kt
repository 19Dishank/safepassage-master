package com.dhruvbuildz.safepassageapp.UI.Home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhruvbuildz.safepassageapp.Database.Room.Model.Category
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.CardViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.CategoryViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.DocumentViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PasswordViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UserViewModel
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.UI.Adapter.CardAdapter
import com.dhruvbuildz.safepassageapp.UI.Adapter.CategoryAdapter
import com.dhruvbuildz.safepassageapp.UI.Adapter.PasswordAdapter
import com.dhruvbuildz.safepassageapp.UI.Adapter.UnifiedAdapter
import com.dhruvbuildz.safepassageapp.databinding.FragmentHomeBinding
import com.dhruvbuildz.safepassageapp.UI.Model.UnifiedItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var passwordAdapter: PasswordAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var cardAdapter: CardAdapter
    private lateinit var unifiedAdapter: UnifiedAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var passwordViewModel: PasswordViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var cardViewModel: CardViewModel
    private lateinit var documentViewModel: DocumentViewModel
    private lateinit var userViewModel: UserViewModel
    private var currentUserId: String? = null
    
    // Store data separately
    private var passwords: List<com.dhruvbuildz.safepassageapp.Database.Room.Model.Password> = emptyList()
    private var cards: List<com.dhruvbuildz.safepassageapp.Database.Room.Model.Card> = emptyList()
    private var documents: List<com.dhruvbuildz.safepassageapp.Database.Room.Model.Document> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        setUpViews()
        setUpMainRcView()
        setUpSearchBar()
        setupCategoryRecyclerView()
        setupCatButtons()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when fragment resumes
        loadAllItems()
    }

    private fun setUpViews() {
        recyclerView = binding.mainRcView
        recyclerView.layoutManager = LinearLayoutManager(context)

        passwordViewModel = (activity as MainActivity).passwordViewModel
        categoryViewModel = (activity as MainActivity).categoryViewModel
        cardViewModel = (activity as MainActivity).cardViewModel
        documentViewModel = (activity as MainActivity).documentViewModel
        userViewModel = (activity as MainActivity).userViewModel

        // Get current user
        userViewModel.getUser().observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                currentUserId = it.userId
                // Once we have the user, observe all data
                observeAllData()
            }
        })

        binding.menuButton.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun setUpMainRcView() {
        passwordAdapter = PasswordAdapter(mutableListOf(), passwordViewModel)
        cardAdapter = CardAdapter(mutableListOf(), cardViewModel)
        
        // Initialize unified adapter
        unifiedAdapter = UnifiedAdapter(
            passwordViewModel = passwordViewModel,
            cardViewModel = cardViewModel,
            onPasswordClick = { password -> handlePasswordClick(password) },
            onCardClick = { card -> handleCardClick(card) },
            onDocumentClick = { document -> handleDocumentClick(document) },
            onPasswordDelete = { password -> handlePasswordDelete(password) },
            onCardDelete = { card -> handleCardDelete(card) },
            onDocumentDelete = { document -> handleDocumentDelete(document) }
        )

        // Set default adapter to unifiedAdapter for "all" view
        binding.mainRcView.adapter = unifiedAdapter
    }

    private fun setUpSearchBar() {
        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                selectedBtnStyle(binding.allBtnTxt, binding.allBtn)
                unSelectedBtnStyle(binding.cardBtnTxt, binding.cardBtn)
                
                // Switch to unified adapter for search
                binding.mainRcView.adapter = unifiedAdapter
                categoryAdapter.clearSelection()
                
                if (s.isNullOrEmpty()) {
                    loadAllItems()
                } else {
                    searchAllItems(s.toString())
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun searchAllItems(searchQuery: String) {
        currentUserId?.let { userId ->
            // Search passwords
            passwordViewModel.searchPassword(searchQuery, userId).observe(viewLifecycleOwner, Observer { passwords ->
                val passwordItems = passwords.map { UnifiedItem.PasswordItem(it) }
                
                // Get current items and update
                val currentItems = unifiedAdapter.currentList.toMutableList()
                currentItems.removeAll { it is UnifiedItem.PasswordItem }
                currentItems.addAll(passwordItems)
                
                unifiedAdapter.submitList(currentItems)
            })
            
            // Search documents
            documentViewModel.searchDocuments(searchQuery, userId).observe(viewLifecycleOwner, Observer { documents ->
                val documentItems = documents.map { UnifiedItem.DocumentItem(it) }
                
                // Get current items and update
                val currentItems = unifiedAdapter.currentList.toMutableList()
                currentItems.removeAll { it is UnifiedItem.DocumentItem }
                currentItems.addAll(documentItems)
                
                unifiedAdapter.submitList(currentItems)
            })
            
            // Note: Cards are excluded from unified search - they only appear in card section
        }
    }

    private fun searchPassword(searchString: String) {
        currentUserId?.let { userId ->
            val searchQuery = "%$searchString%"
            passwordViewModel.searchPassword(searchQuery, userId).observe(viewLifecycleOwner, Observer {
                passwordAdapter.updateItems(it)
            })
        }
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = CategoryAdapter(requireContext(), { category ->
            onCategorySelected(category)
        }, categoryViewModel)

        binding.categoryRCView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }

        categoryViewModel.getAllCategory().observe(viewLifecycleOwner, Observer { categories ->
            categoryAdapter.differ.submitList(categories)
        })
    }

    private fun onCategorySelected(category: Category) {
        currentUserId?.let { userId ->
            passwordViewModel.getPasswordByCategory(category.catId, userId)
                .observe(viewLifecycleOwner) { passwords ->
                    passwordAdapter.updateItems(passwords)
                    binding.mainRcView.adapter = passwordAdapter
                }
        }
        unSelectedBtnStyle(binding.allBtnTxt, binding.allBtn)
        unSelectedBtnStyle(binding.cardBtnTxt, binding.cardBtn)
    }

    private fun selectedBtnStyle(textView: TextView, btnView: RelativeLayout) {
        textView.setTextAppearance(R.style.SelectedBtnText)
        btnView.setBackgroundResource(R.drawable.setecled_button)
    }

    private fun unSelectedBtnStyle(textView: TextView, btnView: RelativeLayout) {
        textView.setTextAppearance(R.style.DeselectedBtnText)
        btnView.setBackgroundResource(R.drawable.deselected_button)
    }

    private fun setupCatButtons() {
        binding.allBtn.setOnClickListener {
            // Load all items (passwords, cards, documents)
            loadAllItems()
            binding.mainRcView.adapter = unifiedAdapter

            selectedBtnStyle(binding.allBtnTxt, binding.allBtn)
            unSelectedBtnStyle(binding.cardBtnTxt, binding.cardBtn)
            categoryAdapter.clearSelection()
        }

        binding.cardBtn.setOnClickListener {
            currentUserId?.let { userId ->
                cardViewModel.getCards(userId).observe(viewLifecycleOwner, Observer { cards ->
                    cardAdapter.updateItems(cards)
                })
            }

            selectedBtnStyle(binding.cardBtnTxt, binding.cardBtn)
            unSelectedBtnStyle(binding.allBtnTxt, binding.allBtn)
            categoryAdapter.clearSelection()
            binding.mainRcView.adapter = cardAdapter
        }
    }

    private fun loadAllItems() {
        // Force refresh all data sources
        currentUserId?.let { userId ->
            passwordViewModel.getAllPasswords(userId)
            cardViewModel.getCards(userId)
            documentViewModel.getAllDocuments(userId)
        }
    }
    
    private fun observeAllData() {
        currentUserId?.let { userId ->
            // Observe passwords
            passwordViewModel.getAllPasswords(userId).observe(viewLifecycleOwner, Observer { passwords ->
                this.passwords = passwords
                updateUnifiedList()
            })
            
            // Observe cards
            cardViewModel.getCards(userId).observe(viewLifecycleOwner, Observer { cards ->
                this.cards = cards
                updateUnifiedList()
            })
            
            // Observe documents
            documentViewModel.getAllDocuments(userId).observe(viewLifecycleOwner, Observer { documents ->
                this.documents = documents
                updateUnifiedList()
            })
        }
    }
    
    fun updateUnifiedList() {
        // Create a combined list from all sources (excluding cards)
        val allItems = mutableListOf<UnifiedItem>()
        
        // Add passwords
        android.util.Log.d("HomeFragment", "Passwords count: ${passwords.size}")
        allItems.addAll(passwords.map { UnifiedItem.PasswordItem(it) })
        
        // Add documents
        android.util.Log.d("HomeFragment", "Documents count: ${documents.size}")
        allItems.addAll(documents.map { UnifiedItem.DocumentItem(it) })
        
        // Note: Cards are excluded from unified view - they only appear in card section
        android.util.Log.d("HomeFragment", "Cards count: ${cards.size} (excluded from unified view)")
        
        android.util.Log.d("HomeFragment", "Total items (passwords + documents): ${allItems.size}")
        
        // Sort items (only passwords and documents)
        val sortedItems = allItems.sortedWith { item1, item2 ->
            when {
                item1 is UnifiedItem.PasswordItem && item2 is UnifiedItem.PasswordItem -> {
                    val date1 = item1.password.createdAt ?: ""
                    val date2 = item2.password.createdAt ?: ""
                    date2.compareTo(date1)
                }
                item1 is UnifiedItem.DocumentItem && item2 is UnifiedItem.DocumentItem -> {
                    val date1 = item1.document.createdAt ?: ""
                    val date2 = item2.document.createdAt ?: ""
                    date2.compareTo(date1)
                }
                else -> 0
            }
        }
        
        android.util.Log.d("HomeFragment", "Submitting ${sortedItems.size} items to adapter")
        unifiedAdapter.submitList(sortedItems)
    }



    // Click handlers
    private fun handlePasswordClick(password: com.dhruvbuildz.safepassageapp.Database.Room.Model.Password) {
        val intent = android.content.Intent(context, com.dhruvbuildz.safepassageapp.UI.ViewLoginActivity::class.java)
        intent.putExtra("PASSWORD_ID", password.passId)
        startActivity(intent)
    }

    private fun handleCardClick(card: com.dhruvbuildz.safepassageapp.Database.Room.Model.Card) {
        val intent = android.content.Intent(context, com.dhruvbuildz.safepassageapp.UI.EditCardActivity::class.java)
        intent.putExtra("CARD_ID", card.carId)
        startActivity(intent)
    }

    private fun handleDocumentClick(document: com.dhruvbuildz.safepassageapp.Database.Room.Model.Document) {
        // Open document using the same logic as ViewDocumentActivity
        try {
            val file = java.io.File(document.filePath)
            if (file.exists()) {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )
                
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                intent.setDataAndType(uri, document.mimeType)
                intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                
                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                } else {
                    // Fallback: try with generic MIME type
                    intent.setDataAndType(uri, "*/*")
                    if (intent.resolveActivity(requireContext().packageManager) != null) {
                        startActivity(intent)
                    } else {
                        android.widget.Toast.makeText(context, "No app found to open this file", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                android.widget.Toast.makeText(context, "File not found", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Error opening document: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Delete handlers
    private fun handlePasswordDelete(password: com.dhruvbuildz.safepassageapp.Database.Room.Model.Password) {
        // Show confirmation dialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Password")
            .setMessage("Are you sure you want to delete '${password.title}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                passwordViewModel.deletePassword(password)
                android.widget.Toast.makeText(context, "Password deleted", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleCardDelete(card: com.dhruvbuildz.safepassageapp.Database.Room.Model.Card) {
        // Show confirmation dialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Card")
            .setMessage("Are you sure you want to delete '${card.title}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                cardViewModel.deleteCard(card)
                android.widget.Toast.makeText(context, "Card deleted", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleDocumentDelete(document: com.dhruvbuildz.safepassageapp.Database.Room.Model.Document) {
        // Show confirmation dialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Document")
            .setMessage("Are you sure you want to delete '${document.title}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                // Delete file from storage
                com.dhruvbuildz.safepassageapp.Fetures.DocumentManager.deleteDocumentFile(document.filePath)
                
                // Delete from database
                documentViewModel.deleteDocument(document)
                
                android.widget.Toast.makeText(context, "Document deleted", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFilterDialog() {
        val options =
            arrayOf(
                "Recently Used",
                "Title (A to Z)",
                "Title (Z to A)",
                "Newest to oldest",
                "Oldest to newest"
            )

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sort Passwords")
        dialog.setItems(options) { dialog, which ->
            when (which) {
                0 -> displayRecent()
                1 -> displayAtoZ()
                2 -> displayZtoA()
                3 -> displayNewToOld()
                4 -> displayOldToNew()
                else -> displayDefault()
            }
        }
        dialog.setNeutralButton("Clear") { dialog, _ ->
            displayDefault()
        }
        dialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        dialog.create().show()

    }

    private fun displayDefault() {
        currentUserId?.let { userId ->
            passwordViewModel.getAllPasswords(userId).observe(viewLifecycleOwner, Observer { passwords ->
                passwordAdapter.updateItems(passwords)
            })
        }
    }

    private fun displayRecent() {
        currentUserId?.let { userId ->
            passwordViewModel.getPasswordsSortedByRecentlyUsed(userId)
                .observe(viewLifecycleOwner, Observer { passwords ->
                    passwordAdapter.updateItems(passwords)
                })
        }
    }

    private fun displayAtoZ() {
        currentUserId?.let { userId ->
            passwordViewModel.getPasswordsSortedByTitle(userId)
                .observe(viewLifecycleOwner, Observer { passwords ->
                    passwordAdapter.updateItems(passwords)
                })
        }
    }

    private fun displayZtoA() {
        currentUserId?.let { userId ->
            passwordViewModel.getPasswordsSortedByTitleDesc(userId)
                .observe(viewLifecycleOwner, Observer { passwords ->
                    passwordAdapter.updateItems(passwords)
                })
        }
    }

    private fun displayNewToOld() {
        currentUserId?.let { userId ->
            passwordViewModel.getPasswordsSortedByNewest(userId)
                .observe(viewLifecycleOwner, Observer { passwords ->
                    passwordAdapter.updateItems(passwords)
                })
        }
    }

    private fun displayOldToNew() {
        currentUserId?.let { userId ->
            passwordViewModel.getPasswordsSortedByOldest(userId)
                .observe(viewLifecycleOwner, Observer { passwords ->
                    passwordAdapter.updateItems(passwords)
                })
        }
    }

}
