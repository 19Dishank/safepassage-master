package com.dhruvbuildz.safepassageapp.UI.Home

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.CardRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.CategoryRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.PasswordRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.PinRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UserRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UtilitiesRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.CardViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.CategoryViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.DocumentViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PasswordViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PinViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UserViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UtilitiesViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.CardVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.CategoryVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.DocumentVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.PasswordVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.PinVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.UserVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.UtilitiesVMFactory
import com.dhruvbuildz.safepassageapp.Fetures.Utils
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.databinding.ActivityMainBinding
import com.dhruvbuildz.safepassageapp.Utils.SessionManager
import com.dhruvbuildz.safepassageapp.Utils.ThemeManager

import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {


    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    lateinit var userViewModel: UserViewModel
    lateinit var passwordViewModel: PasswordViewModel
    lateinit var categoryViewModel: CategoryViewModel
    lateinit var utilitiesViewModel: UtilitiesViewModel
    lateinit var cardViewModel: CardViewModel
    lateinit var documentViewModel: DocumentViewModel
    lateinit var pinViewModel: PinViewModel
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        ThemeManager.initTheme(this)
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)


        setUpViewModel()
        setUpNavigationBar()
        hideNavBarOnKeyboard()

        Utils.screenshotCheck(utilitiesViewModel,this,this)

    }



    private fun setUpNavigationBar() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigation = binding.bottomNavigationBar
        bottomNavigation.setupWithNavController(navController)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }

                R.id.addFragment -> {
                    showAddItemBottomSheet()
                    true
                }

                R.id.passMonitorFragment -> {
                    navController.navigate(R.id.passMonitorFragment)
                    true
                }

                R.id.settingFragment -> {
                    navController.navigate(R.id.settingFragment)
                    true
                }

                else -> false
            }
        }

    }

    private fun showAddItemBottomSheet() {
        val bottomSheet = AddFragment()
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }

    private fun setUpViewModel() {
        val userRepository = UserRepository(SafePassageDatabase(this))
        val viewModelProvider = UserVMFactory(application, userRepository)
        userViewModel = ViewModelProvider(this, viewModelProvider)[UserViewModel::class.java]

        val passwordRepository = PasswordRepository(SafePassageDatabase(this))
        val passViewModelProvider = PasswordVMFactory(application, passwordRepository)
        passwordViewModel =
            ViewModelProvider(this, passViewModelProvider)[PasswordViewModel::class.java]

        val catRepository = CategoryRepository(SafePassageDatabase(this))
        val catViewModelProvider = CategoryVMFactory(application, catRepository)
        categoryViewModel =
            ViewModelProvider(this, catViewModelProvider)[CategoryViewModel::class.java]

        val cardRepository = CardRepository(SafePassageDatabase(this))
        val cardViewModelProvider = CardVMFactory(application, cardRepository)
        cardViewModel =
            ViewModelProvider(this, cardViewModelProvider)[CardViewModel::class.java]

        val utilitiesRepository = UtilitiesRepository(SafePassageDatabase(this))
        val utilitiesViewModelProvider = UtilitiesVMFactory(application, utilitiesRepository)
        utilitiesViewModel =
            ViewModelProvider(this, utilitiesViewModelProvider)[UtilitiesViewModel::class.java]

        val documentViewModelProvider = DocumentVMFactory(SafePassageDatabase(this))
        documentViewModel =
            ViewModelProvider(this, documentViewModelProvider)[DocumentViewModel::class.java]

        val pinViewModelProvider = PinVMFactory(application)
        pinViewModel = ViewModelProvider(this, pinViewModelProvider)[PinViewModel::class.java]
    }

    fun getCurrentUserId(): String? = SessionManager(this).getLastUserId()

    fun refreshData() {
        val userId = getCurrentUserId() ?: return
        passwordViewModel.getAllPasswords(userId)
        cardViewModel.getCards(userId)
        documentViewModel.getAllDocuments(userId)
    }

    private fun hideNavBarOnKeyboard() {
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.root.getWindowVisibleDisplayFrame(rect)
            val screenHeight = binding.root.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            if (keypadHeight > screenHeight * 0.15) {
                binding.bottomNavigationBar.visibility = View.GONE
            } else {
                binding.bottomNavigationBar.visibility = View.VISIBLE
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val currentDestination: NavDestination? = navController.currentDestination
        if (currentDestination != null && currentDestination.id == R.id.homeFragment) {

            MaterialAlertDialogBuilder(this)
                .setTitle("Close App")
                .setMessage("Are you sure you want close app?")
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Close") { dialog, _ ->
                    finishAffinity()
                }
                .show()
        } else {
            navController.navigate(R.id.homeFragment)
        }
    }



}