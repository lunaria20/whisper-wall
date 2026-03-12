package com.example.whisperwall

import android.os.Bundle
import android.widget.TextView
import android.app.Activity

class HomeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Retrieve the username passed from LoginActivity or SignUpActivity
        val username = intent.getStringExtra("USERNAME") ?: "User"

        // Display welcome message with the username
        val tvWelcome  = findViewById<TextView>(R.id.tvWelcomeMessage)
        val tvUsername = findViewById<TextView>(R.id.tvUsername)

        tvWelcome.text  = "Welcome back, $username!"
        tvUsername.text = username
    }
}