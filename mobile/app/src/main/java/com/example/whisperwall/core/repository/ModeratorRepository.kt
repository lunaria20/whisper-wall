package com.example.whisperwall.core.repository

import com.example.whisperwall.core.model.Report
import com.example.whisperwall.core.network.ApiService
import com.example.whisperwall.core.util.arrayOrEmpty
import com.example.whisperwall.core.util.asJsonObjectOrNull
import com.example.whisperwall.core.util.longOrNull
import com.example.whisperwall.core.util.stringOrNull
import com.google.gson.JsonObject

class ModeratorRepository(private val apiService: ApiService) {
    suspend fun getReports(status: String): Result<List<Report>> {
        val response = apiService.getAdminReports(status = status, page = 0, size = 100)
        val payload = response.body()
        if (!response.isSuccessful || payload == null) {
            val fallback = if (response.code() == 403) {
                "You are not allowed to access moderation reports."
            } else {
                "Unable to load reports."
            }
            return Result.failure(IllegalStateException(fallback))
        }

        val reports = payload.arrayOrEmpty("content").mapNotNull { it.asJsonObjectOrNull() }.map { obj ->
            Report(
                id = obj.longOrNull("id") ?: 0L,
                confessionId = obj.longOrNull("confessionId") ?: 0L,
                confessionContent = obj.stringOrNull("confessionContent") ?: "(No confession content available)",
                confessionCategory = obj.stringOrNull("confessionCategory") ?: "Other",
                reason = obj.stringOrNull("reason") ?: "Unspecified",
                description = obj.stringOrNull("description") ?: "No additional details provided.",
                status = obj.stringOrNull("status") ?: "PENDING",
                reportedByUsername = obj.stringOrNull("reportedByUsername") ?: "UNKNOWN",
                createdAt = obj.stringOrNull("createdAt") ?: ""
            )
        }

        return Result.success(reports)
    }

    suspend fun dismiss(reportId: Long): Result<Unit> {
        val response = apiService.dismissReport(reportId)
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(IllegalStateException("Failed to dismiss report."))
    }

    suspend fun removeConfession(reportId: Long): Result<Unit> {
        val response = apiService.removeConfession(reportId)
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(IllegalStateException("Failed to remove confession."))
    }

    suspend fun sendRestrictionRequest(userId: Long, reason: String, durationDays: Int): Result<Unit> {
        val body = JsonObject().apply {
            addProperty("userToRestrictId", userId)
            addProperty("reason", reason)
            addProperty("requestedDurationDays", durationDays)
        }

        val response = apiService.postModeratorRestrictionRequest(body)
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(IllegalStateException("Failed to send restriction request."))
    }
}
