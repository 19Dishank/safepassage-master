package com.dhruvbuildz.safepassageapp.UI

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.PinRepository
import com.dhruvbuildz.safepassageapp.Database.Room.Repository.UserRepository
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PinViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.UserViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.PinVMFactory
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.UserVMFactory
import com.dhruvbuildz.safepassageapp.Fetures.CryptographyManager
import com.dhruvbuildz.safepassageapp.Fetures.HashUtils
import com.dhruvbuildz.safepassageapp.Fetures.Utils
import com.dhruvbuildz.safepassageapp.databinding.ActivitySetPinBinding


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.dhruvbuildz.safepassageapp.Utils.SessionManager
import android.util.Log
import com.dhruvbuildz.safepassageapp.Utils.ApiUrlBuilder
import org.json.JSONObject

class SetPinActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySetPinBinding.inflate(layoutInflater)
    }
    var userId = ""
    private var resolvedUserId: String? = null

    private lateinit var userViewModel: UserViewModel
    private lateinit var pinViewModel: PinViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        setUpViewModel()
        setBackButton()

        // Resolve userId from intent extra
        resolvedUserId = intent.getStringExtra("userId")

        binding.buttonChange.setOnClickListener {
            val pwd1 = binding.pinTextBox1.text.toString()
            val pwd2 = binding.pinTextBox2.text.toString()

            if (pwd1.isEmpty() || pwd2.isEmpty()) {
                Toast.makeText(this, "Please enter pin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pwd1.length < 4 || pwd2.length < 4) {
                Toast.makeText(this, "Pin must be 4 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pwd1 == pwd2) {
                val idForPin = resolvedUserId

                if (!idForPin.isNullOrBlank()) {
                    val cryptographyManager = CryptographyManager(idForPin)
                    val encryptedPwd = cryptographyManager.encryptData(pwd1)

                    // Create or update PIN for this specific user
                    val pin = com.dhruvbuildz.safepassageapp.Database.Room.Model.Pin(
                        userId = idForPin,
                        encryptedPin = encryptedPwd
                    )
                    pinViewModel.insertPin(pin)

                    // Show progress and register PIN in backend MySQL
                    showProgressBar()
                    
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val url = ApiUrlBuilder.getRegisterPinUrl(this@SetPinActivity)
                            
                            // Create proper JSON using JSONObject
                            val jsonObject = org.json.JSONObject()
                            jsonObject.put("userId", idForPin)
                            jsonObject.put("pin", pwd1)
                            val payload = jsonObject.toString()
                            
                            withContext(Dispatchers.Main) {
                                Log.d("SetPinActivity", "Attempting to register PIN in backend: $url")
                                Log.d("SetPinActivity", "JSON payload: $payload")
                            }
                            
                            // Use URL.openConnection() with proper timeout and error handling
                            val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                            conn.requestMethod = "POST"
                            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                            conn.setRequestProperty("User-Agent", "SafePassage-Android/1.0")
                            conn.doOutput = true
                            conn.connectTimeout = 15000 // 15 seconds
                            conn.readTimeout = 15000
                            
                            // Write the JSON payload
                            conn.outputStream.use { os ->
                                val input = payload.toByteArray(Charsets.UTF_8)
                                os.write(input, 0, input.size)
                                os.flush()
                            }
                            
                            // Get response
                            val responseCode = conn.responseCode
                            val responseMessage = conn.responseMessage
                            
                            // Read response body for debugging
                            val responseBody = try {
                                if (responseCode >= 200 && responseCode < 300) {
                                    conn.inputStream?.bufferedReader()?.use { it.readText() } ?: "No response body"
                                } else {
                                    conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details"
                                }
                            } catch (e: Exception) {
                                "Error reading response: ${e.message}"
                            }
                            
                            conn.disconnect()
                            
                            withContext(Dispatchers.Main) {
                                Log.d("SetPinActivity", "Backend PIN registration response: $responseCode - $responseMessage")
                                Log.d("SetPinActivity", "Response body: $responseBody")
                                
                                if (responseCode == 200 || responseCode == 201) {
                                    Log.d("SetPinActivity", "PIN successfully registered in backend MySQL")
                                    
                                    // Verify PIN was stored by checking PIN status
                                    verifyPinStoredInDatabase(idForPin)
                                } else {
                                    Log.e("SetPinActivity", "Backend PIN registration failed with code: $responseCode")
                                    Log.e("SetPinActivity", "Error response: $responseBody")
                                    hideProgressBar()
                                    Toast.makeText(this@SetPinActivity, "Failed to store PIN. Please try again.", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Log.e("SetPinActivity", "Backend PIN registration failed: ${e.message}")
                                Log.e("SetPinActivity", "Exception type: ${e.javaClass.simpleName}")
                                Log.e("SetPinActivity", "Stack trace: ${e.stackTraceToString()}")
                                hideProgressBar()
                                Toast.makeText(this@SetPinActivity, "Network error. Please try again.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    val currentUserId = SessionManager(this).getLastUserId()
                    if (!currentUserId.isNullOrEmpty()) {
                        userViewModel.getUserById(currentUserId).observe(this) { user ->
                            if (user != null) {
                                resolvedUserId = user.userId
                                binding.buttonChange.performClick()
                            } else {
                                Toast.makeText(this, "User not found. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "User not found. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                binding.pinTextBox1.error = "Pin Mismatch"
                binding.pinTextBox2.error = "Pin Mismatch"
            }
        }


        binding.backButton.setOnClickListener {
            finish()
        }

        // Utils.screenshotCheck removed as it's not needed for PIN setup

    }

    private fun setUpViewModel() {
        val userRepository = UserRepository(SafePassageDatabase(this))
        val viewModelProvider = UserVMFactory(application, userRepository)
        userViewModel = ViewModelProvider(this, viewModelProvider)[UserViewModel::class.java]

        val pinViewModelProvider = PinVMFactory(application)
        pinViewModel = ViewModelProvider(this, pinViewModelProvider)[PinViewModel::class.java]
    }

    private fun setBackButton() {
        val idForPin = resolvedUserId
        if (!idForPin.isNullOrBlank()) {
            pinViewModel.hasPin(idForPin).observe(this) { hasPin ->
                binding.backButton.visibility = if (hasPin) View.VISIBLE else View.GONE
            }
        } else {
            binding.backButton.visibility = View.GONE
        }
    }

    private fun verifyPinStoredInDatabase(userId: String) {
        Log.d("SetPinActivity", "Verifying PIN was stored in database for user: $userId")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = ApiUrlBuilder.getCheckPinStatusUrl(this@SetPinActivity)
                
                val jsonObject = org.json.JSONObject()
                jsonObject.put("userId", userId)
                val payload = jsonObject.toString()
                
                withContext(Dispatchers.Main) {
                    Log.d("SetPinActivity", "Verifying PIN storage: $url")
                }
                
                val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("User-Agent", "SafePassage-Android/1.0")
                conn.doOutput = true
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                
                conn.outputStream.use { os ->
                    val input = payload.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                    os.flush()
                }
                
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
                    Log.d("SetPinActivity", "PIN verification response: $responseCode - $responseBody")
                    
                    if (responseCode == 200) {
                        try {
                            val response = org.json.JSONObject(responseBody)
                            if (response.optBoolean("success", false)) {
                                val user = response.optJSONObject("user")
                                val hasPin = user?.optBoolean("hasPin", false) ?: false
                                
                                if (hasPin) {
                                    Log.d("SetPinActivity", "✅ PIN successfully verified in database!")
                                    hideProgressBar()
                                    Toast.makeText(this@SetPinActivity, "PIN Set Successfully", Toast.LENGTH_SHORT).show()
                                    
                                    // Navigate based on context
                                    if (intent.getBooleanExtra("isUpdate", false)) {
                                        finish()
                                    } else {
                                        val intent = Intent(this@SetPinActivity, com.dhruvbuildz.safepassageapp.UI.Home.MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                        finish()
                                    }
                                } else {
                                    Log.e("SetPinActivity", "❌ PIN not found in database after registration!")
                                    hideProgressBar()
                                    Toast.makeText(this@SetPinActivity, "PIN registration failed. Please try again.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Log.e("SetPinActivity", "❌ PIN verification failed: ${response.optString("message")}")
                                hideProgressBar()
                                Toast.makeText(this@SetPinActivity, "PIN verification failed. Please try again.", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Log.e("SetPinActivity", "❌ Error parsing PIN verification response: ${e.message}")
                            hideProgressBar()
                            Toast.makeText(this@SetPinActivity, "PIN verification failed. Please try again.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.e("SetPinActivity", "❌ PIN verification failed with response code: $responseCode")
                        hideProgressBar()
                        Toast.makeText(this@SetPinActivity, "PIN verification failed. Please try again.", Toast.LENGTH_LONG).show()
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("SetPinActivity", "❌ PIN verification error: ${e.message}")
                    hideProgressBar()
                    Toast.makeText(this@SetPinActivity, "PIN verification failed. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonChange.isEnabled = false
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
        binding.buttonChange.isEnabled = true
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        setBackButton()
    }
}