package com.dhruvbuildz.safepassageapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.User
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.CategoryRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.PasswordRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.PinRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UserRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UtilitiesRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.CardViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.CategoryViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PasswordViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PinViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UserViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UtilitiesViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.CategoryVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.PasswordVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.PinVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.UserVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.UtilitiesVMFactory
import com.dhruvbuildz.safepassageapp.Fetures.Utils
import com.dhruvbuildz.safepassageapp.UI.BottomSheet.LockBottomSheet
import com.dhruvbuildz.safepassageapp.UI.Home.MainActivity
import com.dhruvbuildz.safepassageapp.UI.LoginSignUp.Login_Screen
import com.dhruvbuildz.safepassageapp.UI.LoginSignUp.Register_Screen
import com.dhruvbuildz.safepassageapp.databinding.ActivityStartBinding
import com.dhruvbuildz.safepassageapp.Utils.SessionManager
import com.dhruvbuildz.safepassageapp.Utils.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log

class StartActivity : AppCompatActivity() {

    private var _binding: ActivityStartBinding? = null
    private val binding get() = _binding!!

    lateinit var userViewModel: UserViewModel
    lateinit var categoryViewModel: CategoryViewModel
    lateinit var passwordViewModel: PasswordViewModel
    lateinit var cardViewModel: CardViewModel
    lateinit var utilitiesViewModel: UtilitiesViewModel
    lateinit var pinViewModel: PinViewModel

    private var actionDispatched: Boolean = false
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        ThemeManager.initTheme(this)
        
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
            _binding = ActivityStartBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setUpViewModel()
            sessionManager = SessionManager(this)
            
            binding.getStartedBtn.setOnClickListener {
                checkUserAndNavigate()
            }
            
            // Only call screenshotCheck if utilitiesViewModel is initialized
            if (::utilitiesViewModel.isInitialized) {
                Utils.screenshotCheck(utilitiesViewModel, this, this)
            }
        } catch (e: Exception) {
            Log.e("StartActivity", "Error in onCreate: ${e.message}")
            e.printStackTrace()
            // Fallback to basic setup
            try {
                setContentView(binding.root)
                binding.getStartedBtn.setOnClickListener {
                    startActivity(Intent(this, Register_Screen::class.java))
                    finish()
                }
            } catch (fallbackException: Exception) {
                Log.e("StartActivity", "Fallback also failed: ${fallbackException.message}")
                fallbackException.printStackTrace()
            }
        }
    }

    private fun setUpViewModel() {
        try {
            val userRepository = UserRepository(SafePassageDatabase(this))
            val viewModelProvider = UserVMFactory(application, userRepository)
            userViewModel = ViewModelProvider(this, viewModelProvider)[UserViewModel::class.java]

            val categoryRepository = CategoryRepository(SafePassageDatabase(this))
            val catViewModelProvider = CategoryVMFactory(application, categoryRepository)
            categoryViewModel =
                ViewModelProvider(this, catViewModelProvider)[CategoryViewModel::class.java]

            val passwordRepository = PasswordRepository(SafePassageDatabase(this))
            val passViewModelProvider = PasswordVMFactory(application, passwordRepository)
            passwordViewModel =
                ViewModelProvider(this, passViewModelProvider)[PasswordViewModel::class.java]

            val utilitiesRepository = UtilitiesRepository(SafePassageDatabase(this))
            val utilitiesViewModelProvider = UtilitiesVMFactory(application, utilitiesRepository)
            utilitiesViewModel =
                ViewModelProvider(this, utilitiesViewModelProvider)[UtilitiesViewModel::class.java]

            val pinViewModelProvider = PinVMFactory(application)
            pinViewModel = ViewModelProvider(this, pinViewModelProvider)[PinViewModel::class.java]

            val cardRepository = PasswordRepository(SafePassageDatabase(this))
            val cardViewModelProvider = PasswordVMFactory(application, cardRepository)
            cardViewModel = ViewModelProvider(this, cardViewModelProvider)[CardViewModel::class.java]
        } catch (e: Exception) {
            Log.e("StartActivity", "Error setting up ViewModels: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun checkUserAndNavigate() {
        if (actionDispatched) return

        val isLoggedIn = sessionManager.isLoggedIn()
        val lastUserId = sessionManager.getLastUserId()

        if (!isLoggedIn || lastUserId.isNullOrEmpty()) {
            Log.d("StartActivity", "No active session. Redirecting to Login screen.")
            navigateOnce(Intent(this, Login_Screen::class.java))
            return
        }

        Log.d("StartActivity", "Checking stored data for user $lastUserId")
        val liveData = userViewModel.getUserById(lastUserId)
        liveData.observe(this) { user ->
            if (actionDispatched) return@observe

            if (user == null) {
                Log.d("StartActivity", "No local data for user $lastUserId. Redirecting to Login screen.")
                liveData.removeObservers(this)
                navigateOnce(Intent(this, Login_Screen::class.java))
            } else {
                Log.d("StartActivity", "Local user found: ${user.userId}. Proceeding with PIN check.")
                liveData.removeObservers(this)
                checkPinStatusAndNavigate(user.userId)
            }
        }
    }

    private fun navigateOnce(intent: Intent) {
        if (actionDispatched) return
        actionDispatched = true
        startActivity(intent)
        finish()
    }

    private fun checkPinStatusAndNavigate(userId: String) {
        if (actionDispatched) return
        actionDispatched = true
        
        Log.d("StartActivity", "Checking PIN status for user: $userId")
        
        // Check if PIN is set for this specific user
        pinViewModel.hasPin(userId).observe(this) { hasLocalPin ->
            if (hasLocalPin) {
                Log.d("StartActivity", "User has local PIN, showing lock screen")
                // PIN is set locally, show lock screen
                val lockBottomSheet = LockBottomSheet.newInstance(
                    bioLock = true,
                    onBtnClick = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish() // Close StartActivity to prevent back navigation
                    },
                    onCancelled = {
                        // Reset guard so Get Started can be tapped again after cancel
                        actionDispatched = false
                    }
                )
                lockBottomSheet.show(this.supportFragmentManager, lockBottomSheet.tag)
            } else {
                Log.d("StartActivity", "No local PIN found, checking remote backend...")
                // No local PIN — check remote backend to see if user has PIN
                checkRemotePinStatus(userId) { remoteHasPin ->
                    if (remoteHasPin) {
                        Log.d("StartActivity", "Remote PIN found, redirecting to verification")
                        // User has PIN in backend, ask to verify existing PIN
                        val intent = Intent(this, com.dhruvbuildz.safepassageapp.UI.ReinstallVerificationActivity::class.java)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.d("StartActivity", "No PIN found locally or remotely, redirecting to PIN setup")
                        // No PIN anywhere — first-time PIN setup
                        val intent = Intent(this, com.dhruvbuildz.safepassageapp.UI.SetPinActivity::class.java)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun checkRemotePinStatus(userId: String, callback: (Boolean) -> Unit) {
        // Check MySQL backend for PIN status
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = com.dhruvbuildz.safepassageapp.Utils.ApiUrlBuilder.getCheckPinStatusUrl(this@StartActivity)
                Log.d("StartActivity", "Checking PIN status for user $userId at URL: $url")
                
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 15000 // Increased timeout
                connection.readTimeout = 15000
                
                val jsonInputString = "{\"userId\":\"$userId\"}"
                connection.outputStream.use { os ->
                    os.write(jsonInputString.toByteArray())
                }
                
                val responseCode = connection.responseCode
                Log.d("StartActivity", "PIN status check response code: $responseCode")
                
                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("StartActivity", "PIN status response: $response")
                    
                    try {
                        val jsonResponse = org.json.JSONObject(response)
                        val success = jsonResponse.optBoolean("success", false)
                        
                        if (success) {
                            val user = jsonResponse.optJSONObject("user")
                            val hasPin = user?.optBoolean("hasPin", false) ?: false
                            
                            Log.d("StartActivity", "Remote PIN check for user $userId: hasPin=$hasPin")
                            
                            withContext(Dispatchers.Main) {
                                callback(hasPin)
                            }
                        } else {
                            val message = jsonResponse.optString("message", "Unknown error")
                            Log.e("StartActivity", "Remote PIN check failed for user $userId: $message")
                            withContext(Dispatchers.Main) {
                                callback(false)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("StartActivity", "Error parsing PIN status response: ${e.message}")
                        withContext(Dispatchers.Main) {
                            callback(false)
                        }
                    }
                } else {
                    Log.e("StartActivity", "PIN status check failed with response code: $responseCode")
                    // Try to read error response for debugging
                    try {
                        val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details"
                        Log.e("StartActivity", "Error response: $errorResponse")
                    } catch (e: Exception) {
                        Log.e("StartActivity", "Could not read error response: ${e.message}")
                    }
                    withContext(Dispatchers.Main) {
                        callback(false)
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("StartActivity", "Error checking remote PIN: ${e.message}")
                withContext(Dispatchers.Main) {
                    Log.w("StartActivity", "Backend check failed, assuming no PIN to be safe")
                    callback(false)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}