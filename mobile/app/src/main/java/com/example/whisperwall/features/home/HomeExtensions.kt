package com.example.whisperwall.features.home

import android.widget.EditText

fun EditText.validateConfession(): String? {
    val value = text?.toString()?.trim().orEmpty()
    return if (value.isBlank()) "Confession cannot be empty." else null
}

fun String.sanitizeReportReason(): String {
    return trim().ifBlank { "Other" }
}
