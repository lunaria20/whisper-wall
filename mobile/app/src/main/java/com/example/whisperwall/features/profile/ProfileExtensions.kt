package com.example.whisperwall.features.profile

import android.util.Patterns
import android.widget.EditText

fun EditText.validateUsername(): String? {
    val value = text?.toString()?.trim().orEmpty()
    return when {
        value.isBlank() -> "Username is required."
        value.length < 3 -> "Username must be at least 3 characters."
        value.length > 20 -> "Username must not exceed 20 characters."
        else -> null
    }
}

fun EditText.validateEmail(): String? {
    val value = text?.toString()?.trim().orEmpty()
    return when {
        value.isBlank() -> "Email is required."
        !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Please enter a valid email address."
        else -> null
    }
}

fun EditText.validateNewPassword(): String? {
    val value = text?.toString().orEmpty()
    return if (value.length < 8) "New password must be at least 8 characters." else null
}
