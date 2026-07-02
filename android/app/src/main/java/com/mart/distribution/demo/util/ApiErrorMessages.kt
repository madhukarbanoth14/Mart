package com.mart.distribution.demo.util

import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException

object ApiErrorMessages {
    fun fromThrowable(
        e: Throwable,
        fallback: String = "Request failed",
        notFoundFallback: String? = null,
    ): String {
        if (e is HttpException) {
            val nested = parseNestMessage(e)?.trim()?.takeIf { it.isNotEmpty() }
            if (!nested.isNullOrBlank()) return nested
            return when (e.code()) {
                401 -> "Session expired. Please sign in again."
                403 -> "You don't have permission for this action."
                404 -> notFoundFallback ?: "Not found. It may not be available yet."
                400, 422 -> "Please check your input and try again."
                409 -> "This record already exists or conflicts with existing data."
                500 -> "Something went wrong on the server. Please try again."
                else -> fallback
            }
        }
        val msg = e.message?.trim().orEmpty()
        if (msg.isNotBlank() && !looksLikeHttpStatus(msg)) return msg
        return fallback
    }

    private fun parseNestMessage(e: HttpException): String? {
        val body =
            try {
                e.response()?.errorBody()?.string()
            } catch (_: Exception) {
                null
            }
        if (body.isNullOrBlank()) return null
        return try {
            val json = JSONObject(body)
            when (val msg = json.opt("message")) {
                is String -> msg.takeIf { it.isNotBlank() }
                is JSONArray -> {
                    buildString {
                        for (i in 0 until msg.length()) {
                            if (i > 0) append(' ')
                            append(msg.optString(i))
                        }
                    }.trim().takeIf { it.isNotEmpty() }
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun looksLikeHttpStatus(message: String): Boolean =
        message.startsWith("HTTP ", ignoreCase = true) ||
            Regex("^\\d{3}\\s").containsMatchIn(message)
}
