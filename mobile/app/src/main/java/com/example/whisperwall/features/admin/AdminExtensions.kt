package com.example.whisperwall.features.admin

import android.widget.EditText

fun EditText.validateUsername(): String? {
    val value = text?.toString()?.trim().orEmpty()
    return value.validateUsernameText()
}

fun String.validateUsernameText(): String? {
    val value = trim()
    return when {
        value.isBlank() -> "Username is required."
        value.length < 3 -> "Username must be at least 3 characters."
        value.length > 20 -> "Username must be 20 characters or fewer."
        else -> null
    }
}

fun EditText.validateEmail(): String? {
    val value = text?.toString()?.trim().orEmpty()
    return value.validateEmailText()
}

fun String.validateEmailText(): String? {
    val value = trim()
    return when {
        value.isBlank() -> "Email is required."
        !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Please enter a valid email."
        else -> null
    }
}

fun EditText.validatePassword(): String? {
    val value = text?.toString().orEmpty()
    return value.validatePasswordText()
}

fun String.validatePasswordText(): String? {
    val value = trim()
    return when {
        value.isBlank() -> "Password is required."
        value.length < 8 -> "Password must be at least 8 characters."
        else -> null
    }
}

fun EditText.validateDisplayName(): String? {
    val value = text?.toString()?.trim().orEmpty()
    return value.validateDisplayNameText()
}

fun String.validateDisplayNameText(): String? {
    val value = trim()
    return when {
        value.isBlank() -> "Display name is required."
        value.length > 50 -> "Display name must be 50 characters or fewer."
        else -> null
    }
}

fun String.validateRoleName(): String? {
    val value = trim().uppercase()
    return when {
        value.isBlank() -> "Role is required."
        value !in listOf("ROLE_USER", "ROLE_MODERATOR", "ROLE_ADMIN") -> "Invalid role. Use ROLE_USER, ROLE_MODERATOR, or ROLE_ADMIN."
        else -> null
    }
}

fun EditText.validateRestrictionReason(): String? {
    val value = text?.toString()?.trim().orEmpty()
    return value.validateRestrictionReasonText()
}

fun String.validateRestrictionReasonText(): String? {
    val value = trim()
    return when {
        value.isBlank() -> "Reason is required."
        value.length > 500 -> "Reason must be 500 characters or fewer."
        else -> null
    }
}

fun EditText.validateDurationDays(): String? {
    val value = text?.toString()?.trim().orEmpty()
    return value.validateDurationDaysText()
}

fun String.validateDurationDaysText(): String? {
    val value = trim()
    return when {
        value.isBlank() -> null // optional, defaults to 7
        value.toIntOrNull() == null -> "Duration must be a number."
        value.toIntOrNull() ?: 0 <= 0 -> "Duration must be greater than 0."
        value.toIntOrNull() ?: 0 > 365 -> "Duration must not exceed 365 days."
        else -> null
    }
}

fun String.normalizeRoleName(): String {
    val value = trim().uppercase()
    return when (value) {
        "ROLE_MODERATOR" -> "ROLE_MODERATOR"
        "ROLE_ADMIN" -> "ROLE_ADMIN"
        else -> "ROLE_USER"
    }
}
