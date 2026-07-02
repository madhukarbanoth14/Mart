package com.mart.distribution.demo.data.push

import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import com.mart.distribution.demo.data.api.MartApi
import com.mart.distribution.demo.data.api.dto.RegisterFcmTokenRequest
import com.mart.distribution.demo.data.session.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Registers/clears the device FCM token with the backend. All calls are best-effort
 * and silently no-op when Firebase is not configured or the user is not logged in.
 */
class PushTokenRegistrar(
    private val martApi: MartApi,
    private val sessionRepository: SessionRepository,
) {
    /** Fetch the current FCM token and register it (only when logged in). */
    suspend fun registerCurrentToken() {
        if (!sessionRepository.isLoggedIn()) return
        val token = currentToken() ?: return
        register(token)
    }

    /** Register a known token (used from onNewToken). */
    suspend fun register(token: String) {
        if (!sessionRepository.isLoggedIn() || token.isBlank()) return
        runCatching { martApi.registerFcmToken(RegisterFcmTokenRequest(token)) }
    }

    /** Detach this device's token from the user (call before logout). */
    suspend fun unregister() {
        runCatching { martApi.unregisterFcmToken() }
    }

    private suspend fun currentToken(): String? =
        withContext(Dispatchers.IO) {
            runCatching { Tasks.await(FirebaseMessaging.getInstance().token) }.getOrNull()
        }
}
