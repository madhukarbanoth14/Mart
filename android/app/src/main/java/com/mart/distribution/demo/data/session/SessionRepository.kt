package com.mart.distribution.demo.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.mart.distribution.demo.data.demo.LocalDemoAuthConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mart_session")

data class SessionUser(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val companyId: String? = null,
)

class SessionRepository(
    private val context: Context,
    private val gson: Gson,
) {
    private val tokenKey = stringPreferencesKey("access_token")
    private val userKey = stringPreferencesKey("user_json")
    private val localDemoModeKey = booleanPreferencesKey("local_demo_mode")

    /** In-memory token for OkHttp interceptor (avoid blocking I/O on each request). */
    private val tokenCache = AtomicReference<String?>(null)
    private val localDemoCache = AtomicReference(false)
    private val userSnapshot = AtomicReference<SessionUser?>(null)
    private val persistScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val sessionUserFlow: Flow<SessionUser?> =
        context.dataStore.data.map { prefs ->
            prefs[userKey]?.let { json ->
                runCatching { gson.fromJson(json, SessionUser::class.java) }.getOrNull()
            }
        }

    val isLoggedInFlow: Flow<Boolean> = sessionUserFlow.map { it != null }

    suspend fun hydrateTokenCache() {
        val prefs = context.dataStore.data.first()
        tokenCache.set(prefs[tokenKey])
        localDemoCache.set(prefs[localDemoModeKey] == true)
        userSnapshot.set(
            prefs[userKey]?.let { json ->
                runCatching { gson.fromJson(json, SessionUser::class.java) }.getOrNull()
            },
        )
    }

    fun getTokenSnapshot(): String? = tokenCache.get()

    fun getUserSnapshot(): SessionUser? = userSnapshot.get()

    fun isLoggedIn(): Boolean = getUserSnapshot() != null

    fun getUserRole(): String? = getUserSnapshot()?.role

    fun isLocalDemoMode(): Boolean = localDemoCache.get()

    suspend fun saveSession(
        token: String,
        user: SessionUser,
    ) {
        tokenCache.set(token)
        localDemoCache.set(false)
        userSnapshot.set(user)
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = token
            prefs[userKey] = gson.toJson(user)
            prefs[localDemoModeKey] = false
        }
    }

    /** Updates stored user (e.g. after GET /auth/me) without changing the access token. */
    suspend fun patchSessionUser(user: SessionUser) {
        userSnapshot.set(user)
        context.dataStore.edit { prefs ->
            prefs[userKey] = gson.toJson(user)
        }
    }

    suspend fun saveLocalDemoSession(user: SessionUser) {
        tokenCache.set(LocalDemoAuthConfig.LOCAL_DEMO_BEARER_TOKEN)
        localDemoCache.set(true)
        userSnapshot.set(user)
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = LocalDemoAuthConfig.LOCAL_DEMO_BEARER_TOKEN
            prefs[userKey] = gson.toJson(user)
            prefs[localDemoModeKey] = true
        }
    }

    suspend fun clear() {
        invalidateSessionSync()
        context.dataStore.edit { prefs ->
            prefs.remove(tokenKey)
            prefs.remove(userKey)
            prefs[localDemoModeKey] = false
        }
    }

    /** Clears in-memory session immediately; persists to disk without blocking callers. */
    fun invalidateSessionSync() {
        tokenCache.set(null)
        localDemoCache.set(false)
        userSnapshot.set(null)
        persistScope.launch {
            try {
                context.dataStore.edit { prefs ->
                    prefs.remove(tokenKey)
                    prefs.remove(userKey)
                    prefs[localDemoModeKey] = false
                }
            } catch (_: Exception) {
            }
        }
    }
}
