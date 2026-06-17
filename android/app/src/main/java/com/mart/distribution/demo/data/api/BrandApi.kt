package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.api.dto.Brand
import com.mart.distribution.demo.data.api.dto.CreateBrandRequest
import com.mart.distribution.demo.data.api.dto.UpdateBrandRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface BrandApi {
    @GET("brands")
    suspend fun brands(): List<Brand>

    @GET("brands/{id}")
    suspend fun brandById(
        @Path("id") id: String,
    ): Brand

    @POST("brands")
    suspend fun createBrand(
        @Body body: CreateBrandRequest,
    ): Brand

    @PATCH("brands/{id}")
    suspend fun updateBrand(
        @Path("id") id: String,
        @Body body: UpdateBrandRequest,
    ): Brand

    @DELETE("brands/{id}")
    suspend fun deleteBrand(
        @Path("id") id: String,
    ): Brand
}
