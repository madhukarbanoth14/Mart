package com.mart.distribution.demo.data.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor

/**
 * Rewrites each request host to [effectiveBaseUrl] so one APK works on emulator (build default) and
 * real devices (saved override from login screen).
 */
class DynamicBaseUrlInterceptor(
    private val effectiveBaseUrl: () -> String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val base = effectiveBaseUrl().trim().trimEnd('/')
        val target = base.toHttpUrlOrNull() ?: return chain.proceed(request)
        val newUrl =
            request.url.newBuilder()
                .scheme(target.scheme)
                .host(target.host)
                .port(target.port)
                .build()
        return chain.proceed(request.newBuilder().url(newUrl).build())
    }
}
