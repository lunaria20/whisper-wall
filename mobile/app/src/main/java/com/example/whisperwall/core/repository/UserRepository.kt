package com.example.whisperwall.core.repository

import com.example.whisperwall.core.model.UserProfile
import com.example.whisperwall.core.network.ApiService
import com.example.whisperwall.core.util.stringOrNull
import com.google.gson.JsonObject

class UserRepository(private val apiService: ApiService) {
    suspend fun getProfile(): Result<UserProfile> {
        val response = apiService.getMe()
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            return Result.failure(IllegalStateException(extractError(response.code(), body)))
        }

        return Result.success(mapProfile(body))
    }

    suspend fun updateProfile(username: String, email: String, bio: String, profilePicture: String): Result<UserProfile> {
        val request = JsonObject().apply {
            addProperty("username", username.trim())
            addProperty("email", email.trim())
            addProperty("bio", bio.trim())
            addProperty("profilePicture", profilePicture)
        }

        val response = apiService.updateMe(request)
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            return Result.failure(IllegalStateException(extractError(response.code(), body)))
        }

        return Result.success(mapProfile(body))
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        val request = JsonObject().apply {
            addProperty("currentPassword", currentPassword)
            addProperty("newPassword", newPassword)
        }

        val response = apiService.updatePassword(request)
        if (!response.isSuccessful) {
            return Result.failure(IllegalStateException(extractError(response.code(), response.body())))
        }

        return Result.success(Unit)
    }

    private fun mapProfile(body: JsonObject): UserProfile {
        return UserProfile(
            id = body.stringOrNull("id") ?: "",
            username = body.stringOrNull("username") ?: "",
            email = body.stringOrNull("email") ?: "",
            bio = body.stringOrNull("bio") ?: "",
            profilePicture = body.stringOrNull("profilePicture") ?: ""
        )
    }

    private fun extractError(status: Int, payload: JsonObject?): String {
        val nested = payload?.getAsJsonObject("error")
        val message = nested?.stringOrNull("message") ?: payload?.stringOrNull("message")
        if (!message.isNullOrBlank()) return message

        return if (status == 401) "Session expired. Please log in again." else "Unable to process profile request."
    }
}
