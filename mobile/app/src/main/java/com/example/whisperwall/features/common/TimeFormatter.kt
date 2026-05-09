package com.example.whisperwall.features.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimeFormatter {
    private val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun relativeTime(dateIso: String): String {
        if (dateIso.isBlank()) return "Just now"
        return try {
            val created = parser.parse(dateIso) ?: return "Just now"
            val diffMs = Date().time - created.time
            val minutes = diffMs / 60000
            when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "${minutes}m ago"
                minutes < 60 * 24 -> "${minutes / 60}h ago"
                else -> "${minutes / (60 * 24)}d ago"
            }
        } catch (_: Exception) {
            "Just now"
        }
    }
}
