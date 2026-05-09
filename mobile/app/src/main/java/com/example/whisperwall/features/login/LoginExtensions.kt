package com.example.whisperwall.features.login

import android.util.Patterns
import android.widget.EditText

fun EditText.validateLoginIdentifier(): String? {
    val value = text?.toString()?.trim().orEmpty()
    return when {
        value.isBlank() -> "Username or email is required."
        value.contains("@") && !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Please enter a valid email address."
        else -> null
    }
}

fun EditText.validatePassword(): String? {
    val value = text?.toString().orEmpty()
    return when {
        value.isBlank() -> "Password is required."
        value.length < 8 -> "Password must be at least 8 characters."
        else -> null
    }
}
