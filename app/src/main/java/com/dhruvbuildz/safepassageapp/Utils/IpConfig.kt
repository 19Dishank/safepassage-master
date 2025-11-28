package com.dhruvbuildz.safepassageapp.Utils

/**
 * Centralized IP configuration for the app
 * 
 * When your WiFi changes, update ONLY this file:
 * - Line 10: Change CURRENT_IP to your new IP address
 * 
 * Note: network_security_config.xml is now configured to allow
 * all local IPs, so you don't need to update it anymore.
 */
object IpConfig {
    
    // ===== UPDATE ONLY THIS IP ADDRESS =====
    const val CURRENT_IP = "10.68.141.87"
    // ======================================
    
    // Base URLs
    const val BACKEND_BASE_URL = "http://$CURRENT_IP"
    const val BACKEND_API_PATH = "/SafePassage/Admin/dashboard/api"
    
    // Full API URLs
    const val REGISTER_USER_URL = "$BACKEND_BASE_URL$BACKEND_API_PATH/register_user.php"
    const val REGISTER_PIN_URL = "$BACKEND_BASE_URL$BACKEND_API_PATH/register_pin.php"
    const val VERIFY_PIN_URL = "$BACKEND_BASE_URL$BACKEND_API_PATH/verify_pin.php"
    const val CHECK_PIN_STATUS_URL = "$BACKEND_BASE_URL$BACKEND_API_PATH/check_pin_status.php"
    const val USER_LOGIN_URL = "$BACKEND_BASE_URL$BACKEND_API_PATH/user_login.php"
    const val CHECK_USER_STATUS_URL = "$BACKEND_BASE_URL$BACKEND_API_PATH/check_user_status.php"
    
    // Network Security Domains
    val ALLOWED_DOMAINS = listOf(
        CURRENT_IP,
        "10.0.2.2",      // Android Emulator localhost
        "localhost",      // Local development
        "127.0.0.1"      // Local development
    )
}
