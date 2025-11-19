package com.dhruvbuildz.safepassageapp.UI.LoginSignUp

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Model.User
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.PinRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UserRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PinViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UserViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.PinVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.UserVMFactory
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.UI.Home.MainActivity
import com.dhruvbuildz.safepassageapp.UI.SetPinActivity
import com.dhruvbuildz.safepassageapp.databinding.ActivityLoginScreenBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import com.dhruvbuildz.safepassageapp.Utils.ApiUrlBuilder
import com.dhruvbuildz.safepassageapp.Utils.SessionManager
import com.dhruvbuildz.safepassageapp.Utils.ThemeManager


class Login_Screen : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userViewModel: UserViewModel
    private lateinit var pinViewModel: PinViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var database: DatabaseReference

    private val binding by lazy {
        ActivityLoginScreenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        ThemeManager.initTheme(this)
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setUpTheme()
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        setUpViewModel()
        setUpPinViewModel()
        sessionManager = SessionManager(this)

        binding.loginBtn.setOnClickListener {
            hideKeyboard()
            validateLogin()
        }

        binding.regTxt.setOnClickListener {
            startActivity(Intent(this, Register_Screen::class.java))
        }

        adjustPaddingOnKeyboard()
    }


    private fun adjustPaddingOnKeyboard() {
        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            rootView.getWindowVisibleDisplayFrame(r)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight - r.bottom

            val paddingBottom = if (keypadHeight > screenHeight * 0.15) {
                resources.getDimensionPixelSize(R.dimen.padding_300)
            } else {
                resources.getDimensionPixelSize(R.dimen.padding_30)
            }
            binding.constraintLY.setPadding(0, 0, 0, paddingBottom)
        }
    }

    private fun validateLogin() {
        val email = binding.emailBox.text.toString().trim()
        val password = binding.pwdBox.text.toString().trim()

        val emailLayout = binding.emailEditTxtLg
        val passwordLayout = binding.passwordEditTxtLg


        if (email.isEmpty()) {
            emailLayout.error = "Please fill email"
        } else if (password.isEmpty()) {
            passwordLayout.error = "Please fill password"
        } else {
            loginUser(email, password)
        }
        binding.emailBox.addTextChangedListener(
            ClearErrorTextWatcher(
                emailLayout
            )
        )
        binding.pwdBox.addTextChangedListener(
            ClearErrorTextWatcher(
                passwordLayout
            )
        )
    }

    private fun loginUser(email: String, password: String) {
        // Use backend API instead of Firebase for user status checking
        loginWithBackendAPI(email, password)
    }

    private fun loginWithBackendAPI(email: String, password: String) {
        showProgressBar()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = ApiUrlBuilder.getUserLoginUrl(this@Login_Screen)
                
                // Create JSON payload
                val jsonObject = org.json.JSONObject()
                jsonObject.put("email", email)
                jsonObject.put("password", password)
                val payload = jsonObject.toString()
                
                withContext(Dispatchers.Main) {
                    Log.d("Login_Screen", "Attempting backend login: $url")
                    Log.d("Login_Screen", "JSON payload: $payload")
                }
                
                // Make HTTP request to backend
                val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("User-Agent", "SafePassage-Android/1.0")
                conn.doOutput = true
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                
                // Write JSON payload
                conn.outputStream.use { os ->
                    val input = payload.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                    os.flush()
                }
                
                // Get response
                val responseCode = conn.responseCode
                val responseBody = try {
                    if (responseCode >= 200 && responseCode < 300) {
                        conn.inputStream?.bufferedReader()?.use { it.readText() } ?: "{}"
                    } else {
                        conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "{}"
                    }
                } catch (e: Exception) {
                    "{}"
                }
                
                conn.disconnect()
                
                var loginPayload: LoginPayload? = null
                var accountDeactivated = false
                var errorMessage: String? = null

                try {
                    val response = org.json.JSONObject(responseBody)
                    if (responseCode == 200 && response.optBoolean("success", false)) {
                        val userData = response.getJSONObject("user")
                        val userId = userData.getString("userId")
                        val userName = userData.getString("userName")
                        val userEmail = userData.getString("email")
                        val isActive = userData.optBoolean("isActive", true)
                        val lastLogin = userData.optString("lastLogin", "")
                        val loginCount = userData.optInt("loginCount", 0)

                        if (!isActive) {
                            accountDeactivated = true
                        } else {
                            val lastUserId = sessionManager.getLastUserId()
                            if (lastUserId != null && lastUserId != userId) {
                                // Clear local data for the previous user
                                val db = SafePassageDatabase(this@Login_Screen)
                                CoroutineScope(Dispatchers.IO).launch {
                                    db.resetForNewUser()
                                }
                            }
                            sessionManager.markLogin(userId)
                            loginPayload = LoginPayload(
                                userId = userId,
                                userName = userName,
                                userEmail = userEmail,
                                lastLogin = lastLogin,
                                loginCount = loginCount
                            )
                        }
                    } else if (responseCode == 403 && response.optString("code") == "ACCOUNT_DEACTIVATED") {
                        accountDeactivated = true
                    } else {
                        errorMessage = response.optString("message", "Login failed")
                    }
                } catch (e: Exception) {
                    Log.e("Login_Screen", "Error parsing response: ${e.message}")
                    errorMessage = "Login failed. Please try again."
                }

                withContext(Dispatchers.Main) {
                    hideProgressBar()
                    when {
                        loginPayload != null -> {
                            val payload = loginPayload!!
                            val user = User(
                                payload.userId,
                                payload.userEmail,
                                payload.userName,
                                payload.lastLogin.ifEmpty { System.currentTimeMillis().toString() }
                            )
                            userViewModel.insertUser(user)

                            Snackbar.make(binding.root, "Login Successful", Snackbar.LENGTH_SHORT).show()
                            checkUserPinStatusAndNavigate(payload.userId)
                        }
                        accountDeactivated -> {
                            Snackbar.make(
                                binding.root,
                                "Account is deactivated. Please contact administrator.",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            binding.emailEditTxtLg.error = "Invalid Email"
                            binding.passwordEditTxtLg.error = "Invalid Password"
                            Snackbar.make(
                                binding.root,
                                errorMessage ?: "Login failed",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideProgressBar()
                    Log.e("Login_Screen", "Backend login error: ${e.message}")
                    
                    when (e) {
                        is java.net.SocketTimeoutException -> {
                            Snackbar.make(
                                binding.root,
                                "Connection timeout. Please try again.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        is java.net.UnknownHostException -> {
                            Snackbar.make(
                                binding.root,
                                "Network error. Please check your connection.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            Snackbar.make(
                                binding.root,
                                "Network error: ${e.message}",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private data class LoginPayload(
        val userId: String,
        val userName: String,
        val userEmail: String,
        val lastLogin: String,
        val loginCount: Int
    )







    private fun setUpViewModel() {
        val userRepository = UserRepository(SafePassageDatabase(this))
        val viewModelProvider = UserVMFactory(application, userRepository)
        userViewModel = ViewModelProvider(this, viewModelProvider)[UserViewModel::class.java]
    }
    
    private fun setUpPinViewModel() {
        val pinViewModelProvider = PinVMFactory(application)
        pinViewModel = ViewModelProvider(this, pinViewModelProvider)[PinViewModel::class.java]
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }


    private fun setUpTheme() {
        val window: Window = window

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.main)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private class ClearErrorTextWatcher(private val layout: TextInputLayout) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            layout.error = null
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1002 && resultCode == RESULT_OK) {
            // Verification passed — proceed to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    private var verificationLaunched = false

    private fun checkUserPinStatusAndNavigate(userId: String) {
        if (verificationLaunched) return
        verificationLaunched = true
        
        // Check if user has PIN set in local database first
        pinViewModel.getPinByUserId(userId).observe(this) { localPin ->
            if (localPin != null) {
                // User has local PIN, go to verification
                Log.d("Login_Screen", "User has local PIN, going to verification")
                val intent = Intent(this, com.dhruvbuildz.safepassageapp.UI.ReinstallVerificationActivity::class.java)
                intent.putExtra("userId", userId)
                startActivityForResult(intent, 1002)
            } else {
                // No local PIN — check remote backend to see if user has PIN
                Log.d("Login_Screen", "No local PIN found, checking remote backend...")
                checkRemotePinStatus(userId) { remoteHasPin ->
                    if (remoteHasPin) {
                        Log.d("Login_Screen", "Remote PIN found, redirecting to verification")
                        // User has PIN in backend, ask to verify existing PIN
                        val intent = Intent(this, com.dhruvbuildz.safepassageapp.UI.ReinstallVerificationActivity::class.java)
                        intent.putExtra("userId", userId)
                        startActivityForResult(intent, 1002)
                    } else {
                        Log.d("Login_Screen", "No PIN found locally or remotely, redirecting to PIN setup")
                        // No PIN anywhere — first-time PIN setup
                        val intent = Intent(this, com.dhruvbuildz.safepassageapp.UI.SetPinActivity::class.java)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                        finish() // Close login screen since we're going to PIN setup
                    }
                }
            }
        }
    }

    private fun checkRemotePinStatus(userId: String, callback: (Boolean) -> Unit) {
        // Check MySQL backend for PIN status
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = com.dhruvbuildz.safepassageapp.Utils.ApiUrlBuilder.getCheckPinStatusUrl(this@Login_Screen)
                Log.d("Login_Screen", "Checking PIN status for user $userId at URL: $url")
                
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                
                val jsonInputString = "{\"userId\":\"$userId\"}"
                connection.outputStream.use { os ->
                    os.write(jsonInputString.toByteArray())
                }
                
                val responseCode = connection.responseCode
                Log.d("Login_Screen", "PIN status check response code: $responseCode")
                
                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("Login_Screen", "PIN status response: $response")
                    
                    try {
                        val jsonResponse = org.json.JSONObject(response)
                        val success = jsonResponse.optBoolean("success", false)
                        
                        if (success) {
                            val user = jsonResponse.optJSONObject("user")
                            val hasPin = user?.optBoolean("hasPin", false) ?: false
                            
                            Log.d("Login_Screen", "Remote PIN check for user $userId: hasPin=$hasPin")
                            
                            withContext(Dispatchers.Main) {
                                callback(hasPin)
                            }
                        } else {
                            val message = jsonResponse.optString("message", "Unknown error")
                            Log.e("Login_Screen", "Remote PIN check failed for user $userId: $message")
                            withContext(Dispatchers.Main) {
                                callback(false)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Login_Screen", "Error parsing PIN status response: ${e.message}")
                        withContext(Dispatchers.Main) {
                            callback(false)
                        }
                    }
                } else {
                    Log.e("Login_Screen", "PIN status check failed with response code: $responseCode")
                    // Try to read error response for debugging
                    try {
                        val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details"
                        Log.e("Login_Screen", "Error response: $errorResponse")
                    } catch (e: Exception) {
                        Log.e("Login_Screen", "Could not read error response: ${e.message}")
                    }
                    
                    withContext(Dispatchers.Main) {
                        callback(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("Login_Screen", "Error checking remote PIN status: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(false)
                }
            }
        }
    }

}
