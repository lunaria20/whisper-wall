package com.example.whisperwall.features.profile

import com.example.whisperwall.core.repository.UserRepository
import com.example.whisperwall.core.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ProfilePresenter(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ProfileContract.Presenter {

    private var view: ProfileContract.View? = null
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun attach(view: ProfileContract.View) {
        this.view = view
    }

    override fun detach() {
        this.view = null
    }

    override fun loadProfile() {
        presenterScope.launch {
            val result = userRepository.getProfile()
            result.onSuccess { profile ->
                sessionManager.username = profile.username
                sessionManager.email = profile.email
                sessionManager.userId = profile.id
                sessionManager.profilePicture = profile.profilePicture
                view?.showProfile(profile)
            }.onFailure {
                val message = it.message ?: "Unable to load profile."
                view?.showStatus(message)
                if (message.contains("Session expired", ignoreCase = true)) {
                    logout()
                    view?.onSessionExpired()
                }
            }
        }
    }

    override fun saveProfile(username: String, email: String, bio: String, profilePicture: String) {
        presenterScope.launch {
            val result = userRepository.updateProfile(username, email, bio, profilePicture)
            result.onSuccess { profile ->
                sessionManager.username = profile.username
                sessionManager.email = profile.email
                sessionManager.profilePicture = profile.profilePicture
                view?.showProfile(profile)
                view?.showStatus("Profile updated successfully.", true)
            }.onFailure {
                view?.showStatus(it.message ?: "Failed to update profile.")
            }
        }
    }

    override fun changePassword(currentPassword: String, newPassword: String) {
        presenterScope.launch {
            val result = userRepository.changePassword(currentPassword, newPassword)
            result.onSuccess {
                view?.showStatus("Password updated successfully.", true)
            }.onFailure {
                view?.showStatus(it.message ?: "Failed to change password.")
            }
        }
    }

    override fun logout() {
        sessionManager.clear()
    }

    fun destroy() {
        presenterScope.cancel()
    }
}
