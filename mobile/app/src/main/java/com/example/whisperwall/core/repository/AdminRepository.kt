package com.example.whisperwall.core.repository

import com.example.whisperwall.core.network.ApiService
import com.example.whisperwall.core.util.objOrNull
import com.example.whisperwall.core.util.stringOrNull
import com.google.gson.JsonObject

data class AdminUsageStats(
    val totalUsers: Long,
    val totalPosts: Long,
    val totalComments: Long,
    val totalReports: Long,
    val activeUsers: Long,
    val restrictedUsers: Long,
    val adminUsers: Long,
    val moderatorUsers: Long,
)

class AdminRepository(private val apiService: ApiService) {
    suspend fun getUsageStats(): Result<AdminUsageStats> {
        val response = apiService.getAdminUsageStats()
        val payload = response.body()
        if (!response.isSuccessful || payload == null) {
            return Result.failure(IllegalStateException("Unable to load admin statistics."))
        }

        return Result.success(
            AdminUsageStats(
                totalUsers = payload["totalUsers"]?.asLong ?: 0L,
                totalPosts = payload["totalPosts"]?.asLong ?: 0L,
                totalComments = payload["totalComments"]?.asLong ?: 0L,
                totalReports = payload["totalReports"]?.asLong ?: 0L,
                activeUsers = payload["activeUsers"]?.asLong ?: 0L,
                restrictedUsers = payload["restrictedUsers"]?.asLong ?: 0L,
                adminUsers = payload["adminUsers"]?.asLong ?: 0L,
                moderatorUsers = payload["moderatorUsers"]?.asLong ?: 0L,
            )
        )
    }

    suspend fun createUser(username: String, email: String, password: String, displayName: String, roleName: String): Result<Unit> {
        val body = JsonObject().apply {
            addProperty("username", username)
            addProperty("email", email)
            addProperty("password", password)
            addProperty("displayName", displayName)
            addProperty("roleName", roleName)
        }

        val response = apiService.createAdminUser(body)
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(IllegalStateException("Unable to create user."))
    }

    suspend fun deleteUser(userId: Long): Result<Unit> {
        val response = apiService.deleteAdminUser(userId)
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(IllegalStateException("Unable to delete user."))
    }

    suspend fun restrictUser(userId: Long, reason: String, durationDays: Int): Result<Unit> {
        val body = JsonObject().apply {
            addProperty("reason", reason)
            addProperty("durationDays", durationDays)
        }

        val response = apiService.restrictUser(userId, body)
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(IllegalStateException("Unable to restrict user."))
    }

    suspend fun unrestrictUser(userId: Long): Result<Unit> {
        val response = apiService.unrestrictUser(userId)
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(IllegalStateException("Unable to remove restriction."))
    }

    suspend fun deletePost(postId: Long): Result<Unit> {
        val response = apiService.deleteAdminPost(postId)
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(IllegalStateException("Unable to delete post."))
    }

    suspend fun updatePost(postId: Long, content: String, category: String? = null, mood: String? = null): Result<Unit> {
        val body = JsonObject().apply {
            addProperty("content", content)
            if (!category.isNullOrBlank()) addProperty("category", category)
            if (!mood.isNullOrBlank()) addProperty("mood", mood)
        }

        val response = apiService.updateAdminPost(postId, body)
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(IllegalStateException("Unable to update post."))
    }
}