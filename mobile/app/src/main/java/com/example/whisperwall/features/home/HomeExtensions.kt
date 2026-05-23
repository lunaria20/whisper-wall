package com.example.whisperwall.features.home

import android.widget.EditText

fun EditText.validateConfession(): String? {
    val value = text?.toString()?.trim().orEmpty()
    return value.validateConfessionText()
}

fun String.validateConfessionText(): String? {
    val value = trim()
    return when {
        value.isBlank() -> "Confession cannot be empty."
        value.length > 500 -> "Confession must be 500 characters or fewer."
        else -> null
    }
}

fun String.sanitizeReportReason(): String {
    return trim().ifBlank { "Other" }
}
