package com.dhruvbuildz.safepassageapp.Fetures

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import com.dhruvbuildz.safepassageapp.Database.Room.Model.User
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.Utils.IpConfig
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeedbackDialog(private val context: Context, private val user: User) {

    @SuppressLint("InflateParams")
    fun showPageFeedbackDialog(
        layoutInflater: LayoutInflater,
        dialogLayoutId: Int,
        textInputLayoutNameId: Int,
        positiveButtonText: String,
        negativeButtonText: String,
        onSubmitAction: () -> Unit,
        onCancelAction: (() -> Unit)? = null
    ) {
        val dialogView = layoutInflater.inflate(dialogLayoutId, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(dialogView)
        val textInputLayoutName = dialogView.findViewById<TextInputLayout>(textInputLayoutNameId)


        builder.setPositiveButton(positiveButtonText) { _, _ ->
            val name = " ${textInputLayoutName.editText?.text.toString().trim()}"
            if (name.isNotEmpty()) {
                uploadDataToFirebase(name)
                uploadDataToMySQL(name)
                onSubmitAction()
            }
        }

        builder.setNegativeButton(negativeButtonText) { dialog, _ ->
            dialog.cancel()
            onCancelAction?.invoke()
        }

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.color.main)
        dialog.show()
    }

    fun uploadDataToFirebase(text: String) {
        val sugRef = FirebaseDatabase.getInstance().getReference("Feedbacks")
        val suggestionId = sugRef.push().key

        suggestionId?.let { id ->
            val newSuggestionRef = sugRef.child(id)
            newSuggestionRef.child("text").setValue(text)
            newSuggestionRef.child("userName").setValue(user.userName)
            newSuggestionRef.child("userEmail").setValue(user.email)

            val timestamp =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            newSuggestionRef.child("time").setValue(timestamp)
        }
    }
    
    private fun uploadDataToMySQL(text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "${IpConfig.BACKEND_BASE_URL}${IpConfig.BACKEND_API_PATH}/submit_feedback.php"
                Log.d("FeedbackDialog", "Submitting feedback to: $url")
                
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                
                val jsonPayload = JSONObject().apply {
                    put("text", text)
                    put("userName", user.userName)
                    put("userEmail", user.email)
                    put("userId", user.userId)
                }
                
                connection.outputStream.use { os ->
                    os.write(jsonPayload.toString().toByteArray())
                }
                
                val responseCode = connection.responseCode
                val responseBody = if (responseCode == 200) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "{}"
                }
                
                connection.disconnect()
                
                if (responseCode == 200) {
                    val response = JSONObject(responseBody)
                    if (response.optBoolean("success", false)) {
                        Log.d("FeedbackDialog", "Feedback submitted to MySQL successfully")
                    } else {
                        Log.e("FeedbackDialog", "Failed to submit feedback: ${response.optString("message", "Unknown error")}")
                    }
                } else {
                    Log.e("FeedbackDialog", "HTTP error: $responseCode - $responseBody")
                }
            } catch (e: Exception) {
                Log.e("FeedbackDialog", "Error submitting feedback to MySQL: ${e.message}", e)
            }
        }
    }
}