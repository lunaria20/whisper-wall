package com.example.whisperwall

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.app.Activity

class SignUpActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail    = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirm  = findViewById<EditText>(R.id.etConfirmPassword)
        val btnCreate  = findViewById<Button>(R.id.btnCreateAccount)
        val tvLogin    = findViewById<TextView>(R.id.tvLogin)
        val tvError    = findViewById<TextView>(R.id.tvErrorMessage)

        btnCreate.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm  = etConfirm.text.toString().trim()

            // All fields required
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                showError(tvError, "All fields are required.")
                return@setOnClickListener
            }

            // Valid email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError(tvError, "Please enter a valid email address.")
                return@setOnClickListener
            }

            // Password length
            if (password.length < 8) {
                showError(tvError, "Password must be at least 8 characters.")
                return@setOnClickListener
            }

            // Passwords match
            if (password != confirm) {
                showError(tvError, "Passwords do not match.")
                etConfirm.setText("")
                return@setOnClickListener
            }

            // Success — go directly to HomeActivity
            tvError.visibility = View.GONE
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            finish()
        }

        // Go back to LoginActivity
        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Helper function to show error messages
    private fun showError(tv: TextView, message: String) {
        tv.text = message
        tv.visibility = View.VISIBLE
    }
}