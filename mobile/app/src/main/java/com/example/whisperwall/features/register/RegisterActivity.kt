package com.example.whisperwall.features.register

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.app.Activity
import com.example.whisperwall.R
import com.example.whisperwall.core.di.AppContainer

class RegisterActivity : Activity(), RegisterContract.View {
    private val appContainer by lazy { AppContainer.from(this) }
    private lateinit var presenter: RegisterPresenter

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirm: EditText
    private lateinit var btnCreate: Button
    private lateinit var tvError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        presenter = RegisterPresenter(appContainer.authRepository)
        presenter.attach(this)

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirm = findViewById(R.id.etConfirmPassword)
        btnCreate = findViewById(R.id.btnCreateAccount)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        tvError = findViewById(R.id.tvErrorMessage)

        btnCreate.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm = etConfirm.text.toString().trim()

            clearErrors()
            val usernameError = etUsername.validateUsername()
            if (usernameError != null) {
                showUsernameError(usernameError)
                return@setOnClickListener
            }
            val emailError = etEmail.validateEmail()
            val passwordError = etPassword.validatePassword()
            val confirmError = etConfirm.validateMatch(etPassword)

            if (emailError != null) {
                showEmailError(emailError)
                return@setOnClickListener
            }
            if (passwordError != null) {
                showPasswordError(passwordError)
                return@setOnClickListener
            }
            if (confirmError != null) {
                showConfirmPasswordError(confirmError)
                return@setOnClickListener
            }

            presenter.onRegisterClicked(email, password, confirm, username)
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        presenter.detach()
        presenter.destroy()
        super.onDestroy()
    }

    override fun showUsernameError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    override fun showEmailError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    override fun showPasswordError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    override fun showConfirmPasswordError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    override fun clearErrors() {
        tvError.text = ""
        tvError.visibility = View.GONE
    }

    override fun showLoading(isLoading: Boolean) {
        btnCreate.isEnabled = !isLoading
        btnCreate.text = if (isLoading) "Creating account..." else "Create Account"
    }

    override fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    override fun onRegistrationSuccess() {
        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }
}
