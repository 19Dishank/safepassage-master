package com.dhruvbuildz.safepassageapp.Utils

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun markLogin(userId: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_LAST_USER_ID, userId)
        }.apply()
    }

    fun markLogout(clearUserId: Boolean = false) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            if (clearUserId) {
                remove(KEY_LAST_USER_ID)
            }
        }.apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getLastUserId(): String? = prefs.getString(KEY_LAST_USER_ID, null)

    companion object {
        private const val PREF_NAME = "safepassage_session_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LAST_USER_ID = "last_user_id"
    }
}

