package com.example.whisperwall.features.profile

import com.example.whisperwall.core.model.UserProfile

interface ProfileContract {
    interface View {
        fun showProfile(profile: UserProfile)
        fun showStatus(message: String, success: Boolean = false)
        fun onSessionExpired()
        fun onLogoutCompleted()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun loadProfile()
        fun saveProfile(username: String, email: String, bio: String, profilePicture: String)
        fun changePassword(currentPassword: String, newPassword: String)
        fun logout()
    }
}
