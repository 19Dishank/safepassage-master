package com.dhruvbuildz.safepassageapp.UI.LoginSignUp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UserRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UserViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.UserVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.Model.User
import com.dhruvbuildz.safepassageapp.databinding.ActivityRegisterScreenBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.Utils.ApiUrlBuilder
import com.dhruvbuildz.safepassageapp.Utils.ApiUrlTester

import java.util.regex.Pattern

class Register_Screen : AppCompatActivity() {
    private val binding by lazy {
        ActivityRegisterScreenBinding.inflate(layoutInflater)
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        setUpViewModel()
        
        // Debug: Log all API URLs for verification
        ApiUrlTester.logAllApiUrls(this)
        ApiUrlTester.validateApiUrls(this)

        binding.createBtn.setOnClickListener {
            hideKeyboard()
            validateRegistration()
        }

        binding.loginTxt.setOnClickListener {
            hideKeyboard()
            startActivity(Intent(this, Login_Screen::class.java))
            finish()
        }
    }

    private fun setUpViewModel() {
        val userRepository = UserRepository(SafePassageDatabase(this))
        val viewModelProvider = UserVMFactory(application, userRepository)
        userViewModel = ViewModelProvider(this, viewModelProvider)[UserViewModel::class.java]
    }

    private fun validateRegistration() {
        val userName = binding.nameBox.text.toString().trim()
        val email = binding.emailBox.text.toString().trim()
        val password = binding.pwdBox.text.toString().trim()
        val cPassword = binding.cPwdBox.text.toString().trim()

        val nameLayout = binding.nameEditTxtLg
        val emailLayout = binding.emailEditTxtLg
        val passwordLayout = binding.passwordEditTxtLg
        val conformPasswordLayout = binding.conformPasswordEditTxtLg

        var isValid = true

        // Validate email
        if (email.isEmpty()) {
            emailLayout.error = "Please fill email"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailLayout.error = "Invalid email"
            isValid = false
        } else {
            emailLayout.error = null
        }

        // Validate username
        if (userName.isEmpty()) {
            nameLayout.error = "Please fill username"
            isValid = false
        } else if (userName.length < 3) {
            nameLayout.error = "Username must be at least 3 characters long"
            isValid = false
        } else {
            nameLayout.error = null
        }

        // Validate password strength
        if (password.isEmpty()) {
            passwordLayout.error = "Please fill password"
            isValid = false
        } else if (!isValidPassword(password)) {
            passwordLayout.error =
                "Password must be at least 8 characters long and contain at least one digit and one uppercase letter"
            isValid = false
        } else {
            passwordLayout.error = null
        }

        // Confirm password
        if (cPassword.isEmpty()) {
            conformPasswordLayout.error = "Please fill confirm password"
            isValid = false
        } else if (password != cPassword) {
            conformPasswordLayout.error = "Passwords do not match"
            isValid = false
        } else {
            conformPasswordLayout.error = null
        }

        if (isValid) {
            showProgressBar()
            checkIfEmailExists(email, userName, password)
        } else {
            Snackbar.make(
                binding.root,
                "Please fill all required fields correctly.",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        // Clear errors when user starts typing
        binding.nameBox.addTextChangedListener(ClearErrorTextWatcher(nameLayout))
        binding.emailBox.addTextChangedListener(ClearErrorTextWatcher(emailLayout))
        binding.pwdBox.addTextChangedListener(ClearErrorTextWatcher(passwordLayout))
        binding.cPwdBox.addTextChangedListener(ClearErrorTextWatcher(conformPasswordLayout))
    }

    private fun checkIfEmailExists(email: String, name: String, password: String) {
        database.child("users").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        hideProgressBar()
                        binding.emailEditTxtLg.error = "Email already exists."
                        Snackbar.make(
                            binding.root,
                            "Please use a different email.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        registerUser(email, name, password)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    hideProgressBar()
                    Snackbar.make(
                        binding.root,
                        "Failed to check email. Try again.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    Log.e("Register_Screen", "Failed to check email: ${error.message}")
                }
            })
    }

    private fun registerUser(email: String, name: String, password: String) {
        // Use backend API instead of Firebase for registration
        registerWithBackendAPI(email, name, password)
    }

    // Register user with backend API
    private fun registerWithBackendAPI(email: String, name: String, password: String) {
        showProgressBar()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = ApiUrlBuilder.getRegisterUserUrl(this@Register_Screen)
                
                // Generate a unique user ID
                val userId = "user_${System.currentTimeMillis()}_${(0..9999).random()}"
                
                // Create JSON payload
                val jsonObject = org.json.JSONObject()
                jsonObject.put("name", name)
                jsonObject.put("email", email)
                jsonObject.put("password", password)
                jsonObject.put("userId", userId)
                val payload = jsonObject.toString()
                
                withContext(Dispatchers.Main) {
                    Log.d("Register_Screen", "Attempting backend registration: $url")
                    Log.d("Register_Screen", "JSON payload: $payload")
                }
                
                // Make HTTP request to backend
                val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("User-Agent", "SafePassage-Android/1.0")
                conn.doOutput = true
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                
                // Write the JSON payload
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
                
                withContext(Dispatchers.Main) {
                    hideProgressBar()
                    Log.d("Register_Screen", "Backend registration response: $responseCode")
                    Log.d("Register_Screen", "Response body: $responseBody")
                    
                    try {
                        val response = org.json.JSONObject(responseBody)
                        
                        if (responseCode == 200 || responseCode == 201) {
                            // Registration successful
                            Log.d("Register_Screen", "User successfully registered in backend MySQL")
                            
                            Snackbar.make(binding.root, "Account Created Successfully! Please login to continue.", Snackbar.LENGTH_LONG).show()
                            
                            // Navigate to Login screen instead of PIN setup
                            val intent = Intent(this@Register_Screen, com.dhruvbuildz.safepassageapp.UI.LoginSignUp.Login_Screen::class.java)
                            startActivity(intent)
                            finish()
                            
                        } else {
                            // Registration failed
                            val errorMessage = response.optString("message", "Registration failed")
                            Snackbar.make(
                                binding.root,
                                errorMessage,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        
                    } catch (e: Exception) {
                        Log.e("Register_Screen", "Error parsing response: ${e.message}")
                        Snackbar.make(
                            binding.root,
                            "Registration failed. Please try again.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideProgressBar()
                    Log.e("Register_Screen", "Backend registration error: ${e.message}")
                    
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

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Patterns.EMAIL_ADDRESS
        return emailPattern.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        // Password should be at least 8 characters long and contain at least one digit and one uppercase letter
        val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[A-Z]).{8,}$")
        return passwordPattern.matcher(password).matches()
    }

    private class ClearErrorTextWatcher(private val layout: TextInputLayout) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            layout.error = null
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}
