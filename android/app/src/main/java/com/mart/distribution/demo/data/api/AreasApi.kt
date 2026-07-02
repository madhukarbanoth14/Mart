package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.api.dto.AreaDto
import com.mart.distribution.demo.data.api.dto.CreateAreaRequest
import com.mart.distribution.demo.data.api.dto.UpdateAreaRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AreasApi {
    @GET("areas")
    suspend fun areas(): List<AreaDto>

    @POST("areas")
    suspend fun createArea(
        @Body body: CreateAreaRequest,
    ): AreaDto

    @PATCH("areas/{id}")
    suspend fun updateArea(
        @Path("id") id: String,
        @Body body: UpdateAreaRequest,
    ): AreaDto
}
