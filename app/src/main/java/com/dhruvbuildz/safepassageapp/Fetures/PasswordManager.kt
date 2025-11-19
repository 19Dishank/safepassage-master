package com.dhruvbuildz.safepassageapp.Fetures

class PasswordManager {

    fun generatePassword(
        length: Int,
        includeUppercase: Boolean,
        includeLowercase: Boolean,
        includeNumbers: Boolean,
        includeSymbols: Boolean,
        customWords: List<String> = emptyList()
    ): String {
        val uppercaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercaseChars = "abcdefghijklmnopqrstuvwxyz"
        val numbersChars = "0123456789"
        val symbolsChars = "!@#$%^&*()-_=+[]{}|;:,.<>?/"

        val charPool = StringBuilder()
        if (includeUppercase) charPool.append(uppercaseChars)
        if (includeLowercase) charPool.append(lowercaseChars)
        if (includeNumbers) charPool.append(numbersChars)
        if (includeSymbols) charPool.append(symbolsChars)

        if (charPool.isEmpty() && customWords.isEmpty()) {
            throw IllegalArgumentException("No character types selected or custom words provided")
        }

        val password = StringBuilder()

        if (customWords.isNotEmpty()) {
            password.append(customWords.shuffled().joinToString(""))
        }

        val poolString = charPool.toString()
        if (poolString.isEmpty()) {
            throw IllegalArgumentException("Character pool is empty. Ensure at least one character type is selected.")
        }


        while (password.length < length) {
            password.append(poolString.random())
        }

        return password.toString().take(length)
    }


    //Password Strength Checker

    fun getPasswordStrength(password: String): String {
        val length = password.length
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigits = password.any { it.isDigit() }
        val hasSymbols = password.any { !it.isLetterOrDigit() }
        val characterTypes = listOf(hasUppercase, hasLowercase, hasDigits, hasSymbols).count { it }

        val isCommon = isCommonPassword(password)
        val hasRepetitions = password.groupBy { it }.values.any { it.size > 3 }

        return when {
            // Strong password
            length >= 12 && characterTypes >= 4 && !isCommon && !hasRepetitions -> "Strong"

            // Moderate password
            length in 8..11 && characterTypes >= 2 && !isCommon -> "Weak"

            // Weak password
            else -> "Vulnerable"
        }
    }

    // Helper function to check if the password is common or easily guessable
    private fun isCommonPassword(password: String): Boolean {
        val commonPasswords = setOf("123456", "password", "qwerty", "111111", "abc123")
        return password in commonPasswords
    }




}


