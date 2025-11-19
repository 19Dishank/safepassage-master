package com.dhruvbuildz.safepassageapp.Fetures

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import android.util.Log

class CryptographyManager(userId: String) {

    private val KEYSTORE_NAME = "AndroidKeyStore"
    private val ALIAS = "SafePassageKey_$userId"
    private val TRANSFORMATION = "AES/GCM/NoPadding"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"

    init {
        generateSecretKey()
    }

    private fun generateSecretKey() {
        val keyStore = KeyStore.getInstance(KEYSTORE_NAME).apply { load(null) }

        if (!keyStore.containsAlias(ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)  // Set to true if biometric is needed
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_NAME)
        keyStore.load(null)
        return keyStore.getKey(ALIAS, null) as SecretKey
    }

    fun encryptData(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(iv + encryptedData, Base64.DEFAULT)
    }

    // Decrypt the data
    fun decryptData(encryptedData: String): String {
        try {
            val data = Base64.decode(encryptedData, Base64.DEFAULT)
            if (data.size < 12) {
                throw IllegalArgumentException("Invalid encrypted data: Data too short to contain IV.")
            }
            val iv = data.copyOfRange(0, 12)
            val cipherData = data.copyOfRange(12, data.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            val decryptedData = cipher.doFinal(cipherData)
            return String(decryptedData, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("CryptographyManager", "Decryption failed: ${e.message}")
            throw e
        }
    }

}
