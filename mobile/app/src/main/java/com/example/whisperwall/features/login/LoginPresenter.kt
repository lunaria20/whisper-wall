package com.example.whisperwall.features.login

import com.example.whisperwall.core.repository.AuthRepository
import com.example.whisperwall.core.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LoginPresenter(
    private val userRepository: AuthRepository,
    private val sessionManager: SessionManager
) : LoginContract.Presenter {

    private var view: LoginContract.View? = null
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun attach(view: LoginContract.View) {
        this.view = view
    }

    override fun detach() {
        this.view = null
    }

    override fun onLoginClicked(identifier: String, password: String) {
        val currentView = view ?: return
        currentView.clearErrors()

        var hasError = false
        if (password.length < 8) {
            currentView.showPasswordError("Password must be at least 8 characters.")
            hasError = true
        }
        if (hasError) return

        currentView.showLoading(true)
        presenterScope.launch {
            val result = userRepository.login(identifier, password)
            currentView.showLoading(false)
            result.onSuccess { session ->
                sessionManager.token = session.token
                sessionManager.username = if (session.username.isBlank()) {
                    if (identifier.contains("@")) identifier.substringBefore("@") else identifier
                } else session.username
                sessionManager.email = if (session.email.isBlank()) identifier else session.email
                sessionManager.userId = session.userId
                sessionManager.role = session.role
                sessionManager.profilePicture = ""
                currentView.onLoginSuccess(session.role)
            }.onFailure {
                currentView.showError(it.message ?: "Unable to login right now.")
            }
        }
    }

    fun destroy() {
        presenterScope.cancel()
    }
}
