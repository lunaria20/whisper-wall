package com.example.whisperwall.core.model

data class AuthSession(
    val token: String,
    val role: String,
    val username: String,
    val userId: String,
    val email: String
)

data class UserProfile(
    val id: String,
    val username: String,
    val email: String,
    val bio: String,
    val profilePicture: String
)

data class Confession(
    val id: Long,
    val content: String,
    val category: String,
    val username: String,
    val reactionCount: Int,
    val createdAt: String
)

data class Report(
    val id: Long,
    val confessionId: Long,
    val confessionContent: String,
    val confessionCategory: String,
    val reason: String,
    val description: String,
    val status: String,
    val reportedByUsername: String,
    val createdAt: String
)

data class Reaction(
    val userId: String,
    val username: String
)

data class ApiError(
    val message: String
)
