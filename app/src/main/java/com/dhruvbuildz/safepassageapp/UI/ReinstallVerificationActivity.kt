package com.dhruvbuildz.safepassageapp.UI

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dhruvbuildz.safepassageapp.Database.Room.Database.SafePassageDatabase
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.PinViewModel
import com.dhruvbuildz.safepassageapp.Database.Room.ViewModel.VMFactory.PinVMFactory
import com.dhruvbuildz.safepassageapp.Fetures.CryptographyManager
import com.dhruvbuildz.safepassageapp.Fetures.HashUtils
import com.dhruvbuildz.safepassageapp.databinding.ActivitySetPinBinding


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.Utils.ApiUrlBuilder

class ReinstallVerificationActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySetPinBinding.inflate(layoutInflater) }
    private lateinit var pinViewModel: PinViewModel
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Get userId from intent (passed from Login_Screen)
        userId = intent.getStringExtra("userId")
        Log.d("ReinstallVerification", "Received userId from intent: $userId")
        if (userId.isNullOrBlank()) {
            Log.e("ReinstallVerification", "No userId available, finishing activity")
            finish()
            return
        }

        // Set UI to match Set PIN but for verification
        binding.mainHeadText.text = "Verify App Security PIN"
        binding.decText.text = "Enter your existing PIN to continue"
        binding.buttonChange.text = "Verify PIN"
        binding.pinTextBox2.visibility = View.GONE

        // Back button
        binding.backButton.setOnClickListener { finish() }

        // ViewModel
        val pinVMFactory = PinVMFactory(application)
        pinViewModel = ViewModelProvider(this, pinVMFactory)[PinViewModel::class.java]

        binding.buttonChange.setOnClickListener {
            val enteredPin = binding.pinTextBox1.text?.toString()?.trim() ?: ""
            if (enteredPin.length != 4) {
                Toast.makeText(this, "Enter 4-digit PIN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = userId!!
            val cryptographyManager = CryptographyManager(uid)

            // 1) Try local PIN (Room)
            var handled = false
            Log.d("ReinstallVerification", "Checking PIN for userId: $uid")
            pinViewModel.getPinByUserId(uid).observe(this) { pin ->
                if (handled) return@observe
                pin?.let {
                    Log.d("ReinstallVerification", "Found local PIN, verifying...")
                    val decrypted = cryptographyManager.decryptData(it.encryptedPin)
                    if (decrypted == enteredPin) {
                        Log.d("ReinstallVerification", "Local PIN verification successful")
                        handled = true
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Log.d("ReinstallVerification", "Local PIN verification failed, trying remote...")
                        // Try remote next
                        verifyRemote(uid, enteredPin) { ok ->
                            handled = true
                            if (ok) {
                                Log.d("ReinstallVerification", "Remote PIN verification successful")
                                // Persist local PIN so next launches use local PIN (no set again)
                                val encrypted = cryptographyManager.encryptData(enteredPin)
                                pinViewModel.insertPin(
                                    com.dhruvbuildz.safepassageapp.Database.Room.Model.Pin(
                                        userId = uid,
                                        encryptedPin = encrypted
                                    )
                                )
                                setResult(RESULT_OK)
                                finish()
                            } else {
                                Log.d("ReinstallVerification", "Remote PIN verification failed")
                                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } ?: run {
                    Log.d("ReinstallVerification", "No local PIN found, trying remote...")
                    // No local — try remote
                    verifyRemote(uid, enteredPin) { ok ->
                        handled = true
                        if (ok) {
                            Log.d("ReinstallVerification", "Remote PIN verification successful")
                            // Persist local PIN for future unlocks
                            val encrypted = cryptographyManager.encryptData(enteredPin)
                            pinViewModel.insertPin(
                                com.dhruvbuildz.safepassageapp.Database.Room.Model.Pin(
                                    userId = uid,
                                    encryptedPin = encrypted
                                    )
                                )
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            Log.d("ReinstallVerification", "Remote PIN verification failed")
                            Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun verifyRemote(uid: String, enteredPin: String, cb: (Boolean) -> Unit) {
        // Try to verify PIN from backend API first
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = ApiUrlBuilder.getVerifyPinUrl(this@ReinstallVerificationActivity)
                Log.d("ReinstallVerification", "Verifying PIN via backend API: $url")
                
                // Create JSON payload
                val jsonObject = org.json.JSONObject()
                jsonObject.put("userId", uid)
                jsonObject.put("pin", enteredPin)
                val payload = jsonObject.toString()
                Log.d("ReinstallVerification", "PIN verification payload: $payload")
                
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
                    Log.e("ReinstallVerification", "Error reading response: ${e.message}")
                    "{}"
                }
                
                conn.disconnect()
                
                Log.d("ReinstallVerification", "Backend PIN verification response: $responseCode - $responseBody")
                
                withContext(Dispatchers.Main) {
                    try {
                        val response = org.json.JSONObject(responseBody)
                        if (responseCode == 200 && response.optBoolean("success", false)) {
                            Log.d("ReinstallVerification", "Backend PIN verification successful")
                            cb(true)
                        } else {
                            val errorMessage = response.optString("message", "Unknown error")
                            Log.d("ReinstallVerification", "Backend PIN verification failed: $errorMessage")
                            cb(false)
                        }
                    } catch (e: Exception) {
                        Log.e("ReinstallVerification", "Error parsing backend response: ${e.message}")
                        cb(false)
                    }
                }
                
            } catch (e: Exception) {
                Log.e("ReinstallVerification", "Backend PIN verification error: ${e.message}")
                withContext(Dispatchers.Main) {
                    // Backend failed, return false
                    Log.d("ReinstallVerification", "Backend PIN verification failed, no fallback available")
                    cb(false)
                }
            }
        }
    }
}


