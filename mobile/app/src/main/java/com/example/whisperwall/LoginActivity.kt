package com.example.whisperwall

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.app.Activity

class LoginActivity : Activity() {

    // Static credentials for validation
    private val VALID_USERNAME = "whisperuser"
    private val VALID_PASSWORD = "whisper123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername     = findViewById<EditText>(R.id.etUsername)
        val etPassword     = findViewById<EditText>(R.id.etPassword)
        val btnLogin       = findViewById<Button>(R.id.btnLogin)
        val tvSignUp       = findViewById<TextView>(R.id.tvSignUp)
        val tvErrorMessage = findViewById<TextView>(R.id.tvErrorMessage)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Input validation
            if (username.isEmpty() || password.isEmpty()) {
                tvErrorMessage.text = "Please enter both username and password."
                tvErrorMessage.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Static credential check
            if (username == VALID_USERNAME && password == VALID_PASSWORD) {
                tvErrorMessage.visibility = View.GONE
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("USERNAME", username)
                startActivity(intent)
                finish()
            } else {
                tvErrorMessage.text = "Invalid username or password. Please try again."
                tvErrorMessage.visibility = View.VISIBLE
                etPassword.setText("")
            }
        }

        // Navigate to SignUpActivity
        tvSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}