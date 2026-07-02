package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.api.dto.StockAvailabilityDto
import com.mart.distribution.demo.data.api.dto.StockRowDto
import com.mart.distribution.demo.data.api.dto.UpdateStockRequest
import com.mart.distribution.demo.data.api.dto.UpsertStockRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface StockApi {
    @GET("stock")
    suspend fun stock(): List<StockRowDto>

    /** Availability for the signed-in shopkeeper's assigned dealer. */
    @GET("stock/available")
    suspend fun availableStock(): List<StockAvailabilityDto>

    @PATCH("stock/{id}")
    suspend fun updateStock(
        @Path("id") id: String,
        @Body body: UpdateStockRequest,
    ): StockRowDto

    @POST("stock/upsert")
    suspend fun upsertStock(
        @Body body: UpsertStockRequest,
    ): StockRowDto
}
