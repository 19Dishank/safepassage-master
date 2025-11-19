package com.dhruvbuildz.safepassageapp.Fetures

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object HashUtils {
    fun generateSalt(bytes: Int = 16): String {
        val salt = ByteArray(bytes)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hashPin(pin: String, saltBase64: String): String {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val hashed = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashed, Base64.NO_WRAP)
    }
}


