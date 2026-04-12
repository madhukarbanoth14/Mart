package com.mart.distribution.demo.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicReference

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mart_session")

data class SessionUser(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
)

class SessionRepository(
    private val context: Context,
    private val gson: Gson,
) {
    private val tokenKey = stringPreferencesKey("access_token")
    private val userKey = stringPreferencesKey("user_json")

    /** In-memory token for OkHttp interceptor (avoid blocking I/O on each request). */
    private val tokenCache = AtomicReference<String?>(null)
    private val userCache = AtomicReference<SessionUser?>(null)

    val sessionUserFlow: Flow<SessionUser?> =
        context.dataStore.data.map { prefs ->
            prefs[userKey]?.let { gson.fromJson(it, SessionUser::class.java) }
        }

    val isLoggedInFlow: Flow<Boolean> = sessionUserFlow.map { it != null }

    suspend fun hydrateTokenCache() {
        val t = context.dataStore.data.first()[tokenKey]
        tokenCache.set(t)
    }

    fun getTokenSnapshot(): String? = tokenCache.get()

    suspend fun saveSession(
        token: String,
        user: SessionUser,
    ) {
        tokenCache.set(token)
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = token
            prefs[userKey] = gson.toJson(user)
        }
    }

    suspend fun clear() {
        tokenCache.set(null)
        context.dataStore.edit { prefs ->
            prefs.remove(tokenKey)
            prefs.remove(userKey)
        }
    }
}
