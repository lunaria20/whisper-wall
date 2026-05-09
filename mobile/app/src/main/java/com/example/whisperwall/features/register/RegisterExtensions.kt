package com.example.whisperwall.features.register

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

fun EditText.validatePassword(): String? {
    val value = text?.toString().orEmpty()
    return when {
        value.isBlank() -> "Password is required."
        value.length < 8 -> "Password must be at least 8 characters."
        !value.any(Char::isUpperCase) -> "Password must include at least one uppercase letter."
        !value.any(Char::isDigit) -> "Password must include at least one number."
        else -> null
    }
}

fun EditText.validateMatch(other: EditText): String? {
    val value = text?.toString().orEmpty()
    val otherValue = other.text?.toString().orEmpty()
    return if (value == otherValue) null else "Passwords do not match."
}
