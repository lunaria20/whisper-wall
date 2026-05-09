package com.example.whisperwall.core.repository

import com.example.whisperwall.core.model.Confession
import com.example.whisperwall.core.model.Reaction
import com.example.whisperwall.core.network.ApiService
import com.example.whisperwall.core.util.arrayOrEmpty
import com.example.whisperwall.core.util.asJsonObjectOrNull
import com.example.whisperwall.core.util.intOrNull
import com.example.whisperwall.core.util.longOrNull
import com.example.whisperwall.core.util.stringOrNull
import com.google.gson.JsonObject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ConfessionRepository(private val apiService: ApiService) {
    private val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun loadFeed(currentUsername: String, userId: String): Result<List<Confession>> = coroutineScope {
        val publicDeferred = async { apiService.getPublicConfessions(page = 0, size = 50) }
        val mineDeferred = async {
            if (userId.isBlank()) null else apiService.getMyConfessions(userId, page = 0, size = 50)
        }

        val publicResponse = publicDeferred.await()
        val mineResponse = mineDeferred.await()

        val publicItems = mapList(publicResponse.body()?.arrayOrEmpty("content") ?: com.google.gson.JsonArray(), currentUsername)
        val mineItems = mapList(mineResponse?.body()?.arrayOrEmpty("content") ?: com.google.gson.JsonArray(), currentUsername)

        val mergedById = LinkedHashMap<Long, Confession>()
        (mineItems + publicItems).forEach { mergedById[it.id] = it }

        val merged = mergedById.values.sortedByDescending { parseTs(it.createdAt) }
        Result.success(merged)
    }

    suspend fun getLikedConfessionIds(confessions: List<Confession>, userId: String, username: String): Set<Long> {
        val liked = mutableSetOf<Long>()
        confessions.forEach { confession ->
            val result = apiService.getReactions(confession.id)
            if (!result.isSuccessful) return@forEach
            val reactions = result.body()?.mapNotNull { item ->
                val obj = item.asJsonObjectOrNull() ?: return@mapNotNull null
                Reaction(
                    userId = obj.stringOrNull("userId") ?: "",
                    username = obj.stringOrNull("username") ?: ""
                )
            }.orEmpty()

            val currentLiked = reactions.any {
                (userId.isNotBlank() && it.userId == userId) ||
                    (username.isNotBlank() && it.username == username)
            }
            if (currentLiked) liked.add(confession.id)
        }
        return liked
    }

    suspend fun postConfession(content: String, category: String, currentUsername: String): Result<Confession> {
        val body = JsonObject().apply {
            addProperty("content", content.trim())
            addProperty("category", category)
        }
        val response = apiService.createConfession(body)
        val payload = response.body()
        if (!response.isSuccessful || payload == null) {
            return Result.failure(IllegalStateException(parseError(response.code(), payload)))
        }

        return Result.success(mapConfession(payload, currentUsername))
    }

    suspend fun deleteConfession(confessionId: Long): Result<Unit> {
        val response = apiService.deleteConfession(confessionId)
        if (!response.isSuccessful) {
            return Result.failure(IllegalStateException("Failed to delete confession."))
        }
        return Result.success(Unit)
    }

    suspend fun toggleLike(confessionId: Long, currentlyLiked: Boolean): Result<Unit> {
        val response = if (currentlyLiked) {
            apiService.unlikeConfession(confessionId)
        } else {
            val body = JsonObject().apply { addProperty("reactionType", "LIKE") }
            apiService.likeConfession(confessionId, body)
        }

        return if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Unable to update reaction."))
        }
    }

    private fun mapList(array: com.google.gson.JsonArray, currentUsername: String): List<Confession> {
        return array.mapNotNull { it.asJsonObjectOrNull() }
            .map { mapConfession(it, currentUsername) }
    }

    private fun mapConfession(obj: JsonObject, currentUsername: String): Confession {
        return Confession(
            id = obj.longOrNull("id") ?: 0L,
            content = obj.stringOrNull("content") ?: "",
            category = obj.stringOrNull("category") ?: "Other",
            username = obj.stringOrNull("username") ?: currentUsername,
            reactionCount = obj.intOrNull("reactionCount") ?: 0,
            createdAt = obj.stringOrNull("createdAt") ?: ""
        )
    }

    private fun parseTs(value: String): Long {
        return try {
            parser.parse(value)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    private fun parseError(status: Int, payload: JsonObject?): String {
        val message = payload?.getAsJsonObject("error")?.stringOrNull("message") ?: payload?.stringOrNull("message")
        if (!message.isNullOrBlank()) return message
        return if (status == 403) {
            "Your account is temporarily restricted from posting."
        } else {
            "Unable to post confession."
        }
    }
}
