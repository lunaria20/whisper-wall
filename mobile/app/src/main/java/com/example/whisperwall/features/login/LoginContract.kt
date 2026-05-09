package com.example.whisperwall.features.login

interface LoginContract {
    interface View {
        fun showEmailError(message: String)
        fun showPasswordError(message: String)
        fun clearErrors()
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun onLoginSuccess(role: String)
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onLoginClicked(identifier: String, password: String)
    }
}
