package com.example.whisperwall.core.repository

import com.example.whisperwall.core.network.ApiService
import com.example.whisperwall.core.util.objOrNull
import com.example.whisperwall.core.util.stringOrNull
import com.google.gson.JsonArray
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

data class AdminPost(
    val id: Long,
    val content: String,
    val category: String,
    val username: String,
    val createdAt: String,
)

data class AdminUser(
    val id: Long,
    val username: String,
    val email: String,
    val displayName: String,
    val role: String,
    val isRestricted: Boolean,
    val restrictionReason: String?,
    val createdAt: String,
)

data class AdminRestrictionRequest(
    val id: Long,
    val userToRestrict: AdminUser?,
    val requestedByModerator: AdminUser?,
    val reason: String,
    val requestedDurationDays: Int,
    val status: String,
    val createdAt: String,
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

    suspend fun getAllPosts(page: Int = 0, size: Int = 20): Result<List<AdminPost>> {
        val response = apiService.getAllAdminPosts(page, size)
        val payload = response.body()
        if (!response.isSuccessful || payload == null) {
            return Result.failure(IllegalStateException("Unable to load posts."))
        }

        return try {
            val content = payload.getAsJsonArray("content") ?: JsonArray()
            val posts = content.map { element ->
                val obj = element.asJsonObject
                AdminPost(
                    id = obj["id"]?.asLong ?: 0L,
                    content = obj["content"]?.asString ?: "",
                    category = obj["category"]?.asString ?: "",
                    username = obj["username"]?.asString ?: "Unknown",
                    createdAt = obj["createdAt"]?.asString ?: "",
                )
            }
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(page: Int = 0, size: Int = 50): Result<List<AdminUser>> {
        val response = apiService.getAllAdminUsers(page, size)
        val payload = response.body()
        if (!response.isSuccessful || payload == null) {
            return Result.failure(IllegalStateException("Unable to load users."))
        }

        return try {
            val content = payload.getAsJsonArray("content") ?: JsonArray()
            val users = content.map { element ->
                val obj = element.asJsonObject
                AdminUser(
                    id = obj["id"]?.asLong ?: 0L,
                    username = obj["username"]?.asString ?: "",
                    email = obj["email"]?.asString ?: "",
                    displayName = obj["displayName"]?.asString ?: "",
                    role = obj["roleName"]?.asString ?: "ROLE_USER",
                    isRestricted = obj["isRestricted"]?.asBoolean ?: false,
                    restrictionReason = obj["restrictionReason"]?.asString,
                    createdAt = obj["createdAt"]?.asString ?: "",
                )
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingRestrictionRequests(page: Int = 0, size: Int = 20): Result<List<AdminRestrictionRequest>> {
        val response = apiService.getPendingRestrictionRequests(page, size)
        val payload = response.body()
        if (!response.isSuccessful || payload == null) {
            return Result.failure(IllegalStateException("Unable to load restriction requests."))
        }

        return try {
            val content = payload.getAsJsonArray("content") ?: JsonArray()
            val requests = content.map { element ->
                val obj = element.asJsonObject
                val userObj = obj.getAsJsonObject("userToRestrict")
                val moderatorObj = obj.getAsJsonObject("requestedByModerator")
                
                AdminRestrictionRequest(
                    id = obj["id"]?.asLong ?: 0L,
                    userToRestrict = userObj?.let {
                        AdminUser(
                            id = it["id"]?.asLong ?: 0L,
                            username = it["username"]?.asString ?: "",
                            email = it["email"]?.asString ?: "",
                            displayName = it["displayName"]?.asString ?: "",
                            role = it["roleName"]?.asString ?: "ROLE_USER",
                            isRestricted = it["isRestricted"]?.asBoolean ?: false,
                            restrictionReason = it["restrictionReason"]?.asString,
                            createdAt = it["createdAt"]?.asString ?: "",
                        )
                    },
                    requestedByModerator = moderatorObj?.let {
                        AdminUser(
                            id = it["id"]?.asLong ?: 0L,
                            username = it["username"]?.asString ?: "",
                            email = it["email"]?.asString ?: "",
                            displayName = it["displayName"]?.asString ?: "",
                            role = it["roleName"]?.asString ?: "ROLE_USER",
                            isRestricted = it["isRestricted"]?.asBoolean ?: false,
                            restrictionReason = it["restrictionReason"]?.asString,
                            createdAt = it["createdAt"]?.asString ?: "",
                        )
                    },
                    reason = obj["reason"]?.asString ?: "",
                    requestedDurationDays = obj["requestedDurationDays"]?.asInt ?: 7,
                    status = obj["status"]?.asString ?: "PENDING",
                    createdAt = obj["createdAt"]?.asString ?: "",
                )
            }
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveRestrictionRequest(requestId: Long): Result<Unit> {
        val response = apiService.approveRestrictionRequest(requestId, JsonObject())
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(IllegalStateException("Unable to approve restriction."))
    }

    suspend fun rejectRestrictionRequest(requestId: Long, reason: String = ""): Result<Unit> {
        val response = apiService.rejectRestrictionRequest(requestId, reason)
        return if (response.isSuccessful) Result.success(Unit) else Result.failure(IllegalStateException("Unable to reject restriction."))
    }
}