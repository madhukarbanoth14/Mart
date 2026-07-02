package com.mart.distribution.demo.data.session

import com.mart.distribution.demo.data.api.MartApi
import kotlinx.coroutines.flow.Flow

/**
 * App session facade: token + user profile, aligned with backend JWT claims and [GET /auth/me].
 */
class SessionManager(
    private val repository: SessionRepository,
    private val martApi: MartApi,
) {
    val sessionUserFlow: Flow<SessionUser?> = repository.sessionUserFlow

    val isLoggedInFlow: Flow<Boolean> = repository.isLoggedInFlow

    fun getUserRole(): String? = repository.getUserRole()

    fun isLoggedIn(): Boolean = repository.isLoggedIn()

    fun getUserSnapshot(): SessionUser? = repository.getUserSnapshot()

    suspend fun hydrate(): Unit = repository.hydrateTokenCache()

    suspend fun logout() {
        repository.clear()
    }

    /**
     * Persists API login, then refreshes role/company from [MartApi.me] (single source of truth).
     * Skips network refresh in local demo mode.
     */
    suspend fun completeApiLogin(
        accessToken: String,
        userFromLogin: SessionUser,
    ) {
        repository.saveSession(accessToken, userFromLogin)
        refreshProfileFromServerIfNeeded()
    }

    suspend fun refreshProfileFromServerIfNeeded() {
        if (repository.isLocalDemoMode()) return
        try {
            val me = martApi.me()
            val prev = repository.getUserSnapshot() ?: return
            repository.patchSessionUser(
                SessionUser(
                    id = me.userId,
                    name = me.name ?: prev.name,
                    email = me.email,
                    role = me.role,
                    companyId = me.companyId,
                    phone = me.phone,
                    areaName = me.area?.name,
                    assignedDealer = me.assignedDealer,
                    documentUploaded = me.documentUploaded,
                    canPlaceOrders = me.canPlaceOrders,
                    documentStatus = me.documentStatus,
                ),
            )
        } catch (_: Exception) {
            // Keep login payload if /auth/me fails (e.g. flaky network).
        }
    }
}
