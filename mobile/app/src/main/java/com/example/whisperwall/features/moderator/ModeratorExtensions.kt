package com.example.whisperwall.features.moderator

fun String.normalizeReportStatus(): String {
    return when (trim().uppercase()) {
        "REVIEWED" -> "REVIEWED"
        "DISMISSED" -> "DISMISSED"
        else -> "PENDING"
    }
}
