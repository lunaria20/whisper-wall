package com.example.whisperwall.core.util

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

fun JsonObject.stringOrNull(vararg keys: String): String? {
    keys.forEach { key ->
        val element = this.get(key)
        if (element != null && !element.isJsonNull) {
            return try {
                element.asString
            } catch (_: Exception) {
                null
            }
        }
    }
    return null
}

fun JsonObject.intOrNull(vararg keys: String): Int? {
    keys.forEach { key ->
        val element = this.get(key)
        if (element != null && !element.isJsonNull) {
            return try {
                element.asInt
            } catch (_: Exception) {
                null
            }
        }
    }
    return null
}

fun JsonObject.longOrNull(vararg keys: String): Long? {
    keys.forEach { key ->
        val element = this.get(key)
        if (element != null && !element.isJsonNull) {
            return try {
                element.asLong
            } catch (_: Exception) {
                null
            }
        }
    }
    return null
}

fun JsonObject.objOrNull(vararg keys: String): JsonObject? {
    keys.forEach { key ->
        val element = this.get(key)
        if (element != null && element.isJsonObject) {
            return element.asJsonObject
        }
    }
    return null
}

fun JsonObject.arrayOrEmpty(vararg keys: String): JsonArray {
    keys.forEach { key ->
        val element = this.get(key)
        if (element != null && element.isJsonArray) {
            return element.asJsonArray
        }
    }
    return JsonArray()
}

fun JsonElement?.asJsonObjectOrNull(): JsonObject? {
    return if (this != null && this.isJsonObject) this.asJsonObject else null
}
