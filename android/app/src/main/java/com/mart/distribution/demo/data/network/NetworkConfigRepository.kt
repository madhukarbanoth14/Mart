package com.mart.distribution.demo.data.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mart.distribution.demo.BuildConfig
import kotlinx.coroutines.flow.first
import java.util.concurrent.atomic.AtomicReference

private val Context.martNetworkDataStore: DataStore<Preferences> by preferencesDataStore(name = "mart_network")

/**
 * Optional override for API base URL (physical devices). Persisted; read synchronously for OkHttp via cache.
 */
class NetworkConfigRepository(
    private val context: Context,
) {
    private val overrideKey = stringPreferencesKey("api_base_url_override")
    private val overrideCache = AtomicReference<String?>(null)

    fun effectiveBaseUrl(): String {
        if (!BuildConfig.SHOW_BACKEND_URL) {
            return BuildConfig.API_BASE_URL.trimEnd('/')
        }
        val o = overrideCache.get()?.trim().orEmpty()
        if (o.isNotEmpty()) return o.trimEnd('/')
        return BuildConfig.API_BASE_URL.trimEnd('/')
    }

    /** Value to show in the login field after [hydrate]. */
    fun displayUrlForLoginField(): String {
        if (!BuildConfig.SHOW_BACKEND_URL) return ""
        val saved = overrideCache.get()?.trim().orEmpty()
        if (saved.isNotEmpty()) return saved
        val def = BuildConfig.API_BASE_URL.trimEnd('/')
        return if (def.contains("10.0.2.2", ignoreCase = true)) "" else def
    }

    suspend fun hydrate() {
        val raw = context.martNetworkDataStore.data.first()[overrideKey]?.trim().orEmpty()
        overrideCache.set(raw.takeIf { it.isNotEmpty() })
    }

    suspend fun setOverrideFromUserInput(input: String) {
        val trimmed = input.trim().trimEnd('/')
        if (trimmed.isEmpty()) {
            overrideCache.set(null)
            context.martNetworkDataStore.edit { it.remove(overrideKey) }
            return
        }
        overrideCache.set(trimmed)
        context.martNetworkDataStore.edit { it[overrideKey] = trimmed }
    }
}
