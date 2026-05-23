package com.example.whisperwall.features.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.app.Activity
import com.example.whisperwall.R
import com.example.whisperwall.core.di.AppContainer
import com.example.whisperwall.features.admin.AdminActivity
import com.example.whisperwall.features.home.HomeActivity
import com.example.whisperwall.features.moderator.ModeratorActivity
import com.example.whisperwall.features.register.RegisterActivity

class LoginActivity : Activity(), LoginContract.View {
    private val appContainer by lazy { AppContainer.from(this) }
    private lateinit var presenter: LoginPresenter

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView

    private val registerRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_login)

            presenter = LoginPresenter(appContainer.authRepository, appContainer.sessionManager)
            presenter.attach(this)

            etEmail = findViewById(R.id.etUsername)
            etPassword = findViewById(R.id.etPassword)
            btnLogin = findViewById(R.id.btnLogin)
            val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
            tvError = findViewById(R.id.tvErrorMessage)

            btnLogin.setOnClickListener {
                val identifier = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val identifierError = etEmail.validateLoginIdentifier()
                val passwordError = etPassword.validatePassword()

                clearErrors()
                if (identifierError != null) {
                    showEmailError(identifierError)
                    return@setOnClickListener
                }
                if (passwordError != null) {
                    showPasswordError(passwordError)
                    return@setOnClickListener
                }

                presenter.onLoginClicked(identifier, password)
            }

            tvSignUp.setOnClickListener {
                startActivityForResult(Intent(this, RegisterActivity::class.java), registerRequestCode)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
            android.util.Log.e("LoginActivity", "Initialization error", e)
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == registerRequestCode && resultCode == RESULT_OK) {
            Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        presenter.detach()
        presenter.destroy()
        super.onDestroy()
    }

    override fun showEmailError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    override fun showPasswordError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    override fun clearErrors() {
        tvError.text = ""
        tvError.visibility = View.GONE
    }

    override fun showLoading(isLoading: Boolean) {
        btnLogin.isEnabled = !isLoading
        btnLogin.text = if (isLoading) "Logging in..." else "Log In"
    }

    override fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
        etPassword.setText("")
    }

    override fun onLoginSuccess(role: String) {
        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
        val next = if (role.equals("ADMIN", ignoreCase = true) || role.equals("ROLE_ADMIN", ignoreCase = true)) {
            Intent(this, AdminActivity::class.java)
        } else if (role.equals("MODERATOR", ignoreCase = true) || role.equals("ROLE_MODERATOR", ignoreCase = true)) {
            Intent(this, ModeratorActivity::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }
        startActivity(next)
        finish()
    }
}
