package com.mart.distribution.demo.data.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.martOnboardingDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "mart_onboarding")

/** Remembers whether the user has seen the intro carousel (shown once). */
class OnboardingPreferences(
    private val context: Context,
) {
    private val completedKey = booleanPreferencesKey("has_completed_onboarding")

    suspend fun hasCompleted(): Boolean =
        context.martOnboardingDataStore.data.first()[completedKey] ?: false

    suspend fun markCompleted() {
        context.martOnboardingDataStore.edit { it[completedKey] = true }
    }
}
