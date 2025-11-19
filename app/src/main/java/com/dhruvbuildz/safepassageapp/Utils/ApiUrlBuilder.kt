package com.dhruvbuildz.safepassageapp.Utils

import android.content.Context
import com.dhruvbuildz.safepassageapp.R
import com.dhruvbuildz.safepassageapp.Utils.IpConfig

/**
 * Utility class to build API URLs dynamically
 * This eliminates the need for hardcoded URLs and makes the app more maintainable
 */
object ApiUrlBuilder {
    
    /**
     * Builds a complete API URL by combining base URL, API path, and endpoint
     */
    fun buildUrl(context: Context, endpoint: String): String {
        return "${IpConfig.BACKEND_BASE_URL}${IpConfig.BACKEND_API_PATH}/$endpoint"
    }
    
    /**
     * Builds URL for user registration
     */
    fun getRegisterUserUrl(context: Context): String {
        return IpConfig.REGISTER_USER_URL
    }
    
    /**
     * Builds URL for PIN registration
     */
    fun getRegisterPinUrl(context: Context): String {
        return IpConfig.REGISTER_PIN_URL
    }
    
    /**
     * Builds URL for PIN verification
     */
    fun getVerifyPinUrl(context: Context): String {
        return IpConfig.VERIFY_PIN_URL
    }
    
    /**
     * Builds URL for checking PIN status
     */
    fun getCheckPinStatusUrl(context: Context): String {
        return IpConfig.CHECK_PIN_STATUS_URL
    }
    
    /**
     * Builds URL for user login
     */
    fun getUserLoginUrl(context: Context): String {
        return IpConfig.USER_LOGIN_URL
    }
    
    /**
     * Builds URL for checking user status
     */
    fun getCheckUserStatusUrl(context: Context): String {
        return IpConfig.CHECK_USER_STATUS_URL
    }
    
    /**
     * Gets the base URL for the backend
     */
    fun getBaseUrl(context: Context): String {
        return IpConfig.BACKEND_BASE_URL
    }
    
    /**
     * Gets the API path
     */
    fun getApiPath(context: Context): String {
        return IpConfig.BACKEND_API_PATH
    }
    
    /**
     * Builds a custom API URL with a custom endpoint
     */
    fun buildCustomUrl(context: Context, customEndpoint: String): String {
        return buildUrl(context, customEndpoint)
    }
}
