package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.api.dto.StockRowDto
import retrofit2.http.GET

interface StockApi {
    @GET("stock")
    suspend fun stock(): List<StockRowDto>
}
