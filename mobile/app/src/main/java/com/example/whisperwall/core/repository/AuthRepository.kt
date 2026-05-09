package com.example.whisperwall.core.repository

import com.example.whisperwall.core.model.ApiError
import com.example.whisperwall.core.model.AuthSession
import com.example.whisperwall.core.network.ApiService
import com.example.whisperwall.core.util.objOrNull
import com.example.whisperwall.core.util.stringOrNull
import com.google.gson.JsonObject

class AuthRepository(private val apiService: ApiService) {
    suspend fun login(usernameOrEmail: String, password: String): Result<AuthSession> {
        val body = JsonObject().apply {
            addProperty("username", usernameOrEmail)
            addProperty("email", usernameOrEmail)
            addProperty("password", password)
        }

        return toAuthResult(apiService.login(body))
    }

    suspend fun register(username: String, email: String, password: String): Result<AuthSession> {
        val body = JsonObject().apply {
            addProperty("username", username)
            addProperty("email", email)
            addProperty("password", password)
        }

        return toAuthResult(apiService.register(body))
    }

    private fun toAuthResult(response: retrofit2.Response<JsonObject>): Result<AuthSession> {
        val payload = response.body()
        val nested = payload?.objOrNull("data")
        val source = nested ?: payload

        if (!response.isSuccessful || source == null) {
            return Result.failure(IllegalStateException(parseError(response.code(), payload)))
        }

        val token = source.stringOrNull("token") ?: ""
        val username = source.stringOrNull("username") ?: ""
        val email = source.stringOrNull("email") ?: ""
        val role = source.stringOrNull("role") ?: "USER"
        val userId = source.stringOrNull("id") ?: ""

        if (token.isBlank()) {
            return Result.failure(IllegalStateException("Authentication token is missing."))
        }

        return Result.success(AuthSession(token, role, username, userId, email))
    }

    private fun parseError(status: Int, payload: JsonObject?): String {
        val nestedError = payload?.objOrNull("error")
        val message = nestedError?.stringOrNull("message") ?: payload?.stringOrNull("message")
        if (!message.isNullOrBlank()) return message

        return when (status) {
            400 -> "Invalid input. Please check your details."
            401 -> "Invalid credentials. Please try again."
            403 -> "Access denied."
            404 -> "Service not found."
            409 -> "Username or email already exists."
            500 -> "Server error. Please try again later."
            else -> ApiError("Something went wrong. Please try again.").message
        }
    }
}
