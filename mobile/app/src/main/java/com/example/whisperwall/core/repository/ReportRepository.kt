package com.example.whisperwall.core.repository

import com.example.whisperwall.core.network.ApiService
import com.example.whisperwall.core.util.stringOrNull
import com.google.gson.JsonObject

class ReportRepository(private val apiService: ApiService) {
    suspend fun submitReport(confessionId: Long, reason: String): Result<Unit> {
        val body = JsonObject().apply {
            addProperty("reason", reason.trim())
        }

        val response = apiService.reportConfession(confessionId, body)
        if (response.isSuccessful) {
            return Result.success(Unit)
        }

        val message = response.body()?.getAsJsonObject("error")?.stringOrNull("message")
            ?: if (response.code() == 409) "You have already reported this confession." else "Unable to submit report."

        return Result.failure(IllegalStateException(message))
    }
}
