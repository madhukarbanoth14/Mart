package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.api.dto.AreaDto
import retrofit2.http.GET

interface AreasApi {
    @GET("areas")
    suspend fun areas(): List<AreaDto>
}
