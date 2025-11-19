package com.dhruvbuildz.safepassageapp.Utils

import android.content.Context
import android.util.Log

/**
 * Utility class to test and verify API URLs are built correctly
 * This helps debug the refactored URL system
 */
object ApiUrlTester {
    
    private const val TAG = "ApiUrlTester"
    
    /**
     * Logs all API URLs for debugging purposes
     */
    fun logAllApiUrls(context: Context) {
        Log.d(TAG, "=== API URL Configuration ===")
        Log.d(TAG, "Base URL: ${ApiUrlBuilder.getBaseUrl(context)}")
        Log.d(TAG, "API Path: ${ApiUrlBuilder.getApiPath(context)}")
        Log.d(TAG, "")
        Log.d(TAG, "=== Complete API URLs ===")
        Log.d(TAG, "Register User: ${ApiUrlBuilder.getRegisterUserUrl(context)}")
        Log.d(TAG, "Register PIN: ${ApiUrlBuilder.getRegisterPinUrl(context)}")
        Log.d(TAG, "Verify PIN: ${ApiUrlBuilder.getVerifyPinUrl(context)}")
        Log.d(TAG, "Check PIN Status: ${ApiUrlBuilder.getCheckPinStatusUrl(context)}")
        Log.d(TAG, "User Login: ${ApiUrlBuilder.getUserLoginUrl(context)}")
        Log.d(TAG, "Check User Status: ${ApiUrlBuilder.getCheckUserStatusUrl(context)}")
        Log.d(TAG, "=============================")
    }
    
    /**
     * Validates that URLs are properly formatted
     */
    fun validateApiUrls(context: Context): Boolean {
        try {
            val urls = listOf(
                ApiUrlBuilder.getRegisterUserUrl(context),
                ApiUrlBuilder.getRegisterPinUrl(context),
                ApiUrlBuilder.getVerifyPinUrl(context),
                ApiUrlBuilder.getCheckPinStatusUrl(context),
                ApiUrlBuilder.getUserLoginUrl(context),
                ApiUrlBuilder.getCheckUserStatusUrl(context)
            )
            
            for (url in urls) {
                if (!url.startsWith("http")) {
                    Log.e(TAG, "Invalid URL format: $url")
                    return false
                }
                if (!url.contains(".php")) {
                    Log.e(TAG, "Missing .php extension: $url")
                    return false
                }
            }
            
            Log.d(TAG, "All API URLs are valid!")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating URLs: ${e.message}")
            return false
        }
    }
}
