package com.example.whisperwall.features.register

import com.example.whisperwall.core.repository.AuthRepository
import android.util.Patterns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class RegisterPresenter(
    private val userRepository: AuthRepository
) : RegisterContract.Presenter {

    private var view: RegisterContract.View? = null
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun attach(view: RegisterContract.View) {
        this.view = view
    }

    override fun detach() {
        this.view = null
    }

    override fun onRegisterClicked(email: String, password: String, confirmPassword: String, username: String) {
        val currentView = view ?: return
        currentView.clearErrors()

        var hasError = false

        val normalizedUsername = username.trim()
        val normalizedEmail = email.trim()

        if (normalizedUsername.isBlank()) {
            currentView.showUsernameError("Username is required.")
            hasError = true
        } else if (normalizedUsername.length < 3) {
            currentView.showUsernameError("Username must be at least 3 characters.")
            hasError = true
        } else if (normalizedUsername.length > 20) {
            currentView.showUsernameError("Username must not exceed 20 characters.")
            hasError = true
        }
        if (normalizedEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
            currentView.showEmailError("Please enter a valid email address.")
            hasError = true
        }
        if (password.length < 8) {
            currentView.showPasswordError("Password must be at least 8 characters.")
            hasError = true
        } else if (!password.any(Char::isUpperCase)) {
            currentView.showPasswordError("Password must include at least one uppercase letter.")
            hasError = true
        } else if (!password.any(Char::isDigit)) {
            currentView.showPasswordError("Password must include at least one number.")
            hasError = true
        }
        if (password != confirmPassword) {
            currentView.showConfirmPasswordError("Passwords do not match.")
            hasError = true
        }

        if (hasError) return

        currentView.showLoading(true)
        presenterScope.launch {
            val registerResult = userRepository.register(normalizedUsername, normalizedEmail, password)
            currentView.showLoading(false)
            registerResult.onSuccess {
                currentView.onRegistrationSuccess()
            }.onFailure {
                currentView.showError(it.message ?: "Unable to create account.")
            }
        }
    }

    fun destroy() {
        presenterScope.cancel()
    }
}
