package com.mart.distribution.demo

import android.content.Context
import com.mart.distribution.demo.data.api.AuthInterceptor
import com.mart.distribution.demo.data.api.MartApi
import com.mart.distribution.demo.data.cart.CartRepository
import com.mart.distribution.demo.data.session.SessionRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    val gson: Gson = GsonBuilder().serializeNulls().create()

    val sessionRepository = SessionRepository(context, gson)

    val cartRepository = CartRepository()

    private val logging =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    private val okHttp: OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionRepository))
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    private val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL.trimEnd('/') + "/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    val martApi: MartApi = retrofit.create(MartApi::class.java)
}
