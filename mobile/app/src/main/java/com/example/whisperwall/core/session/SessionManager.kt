package com.example.whisperwall.core.session

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var token: String
        get() = prefs.getString(KEY_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var username: String
        get() = prefs.getString(KEY_USERNAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    var userId: String
        get() = prefs.getString(KEY_USER_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var email: String
        get() = prefs.getString(KEY_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    var role: String
        get() = prefs.getString(KEY_ROLE, "USER") ?: "USER"
        set(value) = prefs.edit().putString(KEY_ROLE, value).apply()

    var profilePicture: String
        get() = prefs.getString(KEY_PROFILE_PICTURE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PROFILE_PICTURE, value).apply()

    fun isLoggedIn(): Boolean = token.isNotBlank()

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "whisperwall_session"
        private const val KEY_TOKEN = "ww_token"
        private const val KEY_USERNAME = "ww_username"
        private const val KEY_USER_ID = "ww_user_id"
        private const val KEY_EMAIL = "ww_user_email"
        private const val KEY_ROLE = "ww_user_role"
        private const val KEY_PROFILE_PICTURE = "ww_profile_picture"
    }
}
