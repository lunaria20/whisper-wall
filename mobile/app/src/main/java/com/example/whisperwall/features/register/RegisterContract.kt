package com.example.whisperwall.features.register

interface RegisterContract {
    interface View {
        fun showUsernameError(message: String)
        fun showEmailError(message: String)
        fun showPasswordError(message: String)
        fun showConfirmPasswordError(message: String)
        fun clearErrors()
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun onRegistrationSuccess()
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun onRegisterClicked(email: String, password: String, confirmPassword: String, username: String)
    }
}
