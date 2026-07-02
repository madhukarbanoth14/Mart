package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.api.dto.OrderingConfigDto
import retrofit2.http.GET

interface ConfigApi {
    @GET("config/ordering")
    suspend fun orderingConfig(): OrderingConfigDto
}
