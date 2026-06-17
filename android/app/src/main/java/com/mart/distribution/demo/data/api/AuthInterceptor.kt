package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.session.SessionRepository
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches Bearer token for protected routes. Login path is skipped.
 * Clears session on 401 so the next launch returns to login.
 */
class AuthInterceptor(
    private val sessionRepository: SessionRepository,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url.encodedPath.contains("auth/login")) {
            return chain.proceed(request)
        }
        val token = sessionRepository.getTokenSnapshot()
        val skipBearer = sessionRepository.isLocalDemoMode()
        val newRequest =
            when {
                skipBearer || token.isNullOrBlank() -> request
                else -> request.newBuilder().header("Authorization", "Bearer $token").build()
            }
        val response = chain.proceed(newRequest)
        if (response.code == 401 &&
            !sessionRepository.isLocalDemoMode() &&
            !request.url.encodedPath.contains("auth/login")
        ) {
            sessionRepository.invalidateSessionSync()
        }
        return response
    }
}
