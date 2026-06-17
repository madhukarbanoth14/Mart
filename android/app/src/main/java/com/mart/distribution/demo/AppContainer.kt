package com.mart.distribution.demo

import android.content.Context
import com.mart.distribution.demo.data.api.AuthInterceptor
import com.mart.distribution.demo.data.api.DynamicBaseUrlInterceptor
import com.mart.distribution.demo.data.api.MartApi
import com.mart.distribution.demo.data.brands.BrandsRepository
import com.mart.distribution.demo.data.cart.CartRepository
import com.mart.distribution.demo.data.demo.DemoFlowRepository
import com.mart.distribution.demo.data.demo.LocalDemoMartStore
import com.mart.distribution.demo.data.network.NetworkConfigRepository
import com.mart.distribution.demo.data.session.SessionManager
import com.mart.distribution.demo.data.session.SessionRepository
import com.mart.distribution.demo.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    val applicationContext: Context = context.applicationContext

    val gson: Gson = GsonBuilder().serializeNulls().create()

    val sessionRepository = SessionRepository(applicationContext, gson)

    val networkConfigRepository = NetworkConfigRepository(applicationContext)

    val cartRepository = CartRepository()

    val demoFlowRepository = DemoFlowRepository()

    val localDemoMartStore = LocalDemoMartStore()

    private val logging =
        HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
        }

    private val httpCache =
        Cache(
            File(applicationContext.cacheDir, "mart_http_cache"),
            10L * 1024 * 1024,
        )

    private val okHttp: OkHttpClient =
        OkHttpClient.Builder()
            .cache(httpCache)
            .addInterceptor(DynamicBaseUrlInterceptor(networkConfigRepository::effectiveBaseUrl))
            .addInterceptor(AuthInterceptor(sessionRepository))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    private val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL.trimEnd('/') + "/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    val martApi: MartApi = retrofit.create(MartApi::class.java)

    val sessionManager = SessionManager(sessionRepository, martApi)
    val brandsRepository = BrandsRepository(martApi, sessionRepository, localDemoMartStore)
}
