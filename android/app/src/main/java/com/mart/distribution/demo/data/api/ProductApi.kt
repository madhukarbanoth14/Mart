package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.data.api.dto.ProductsPagedResponse
import com.mart.distribution.demo.data.api.dto.CreateProductRequest
import com.mart.distribution.demo.data.api.dto.UpdateProductRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {
    @GET("products")
    suspend fun products(
        @Query("search") search: String? = null,
        @Query("brandId") brandId: String? = null,
        @Query("shelf") shelf: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
    ): List<ProductDto>

    @GET("products/paged")
    suspend fun productsPaged(
        @Query("search") search: String? = null,
        @Query("brandId") brandId: String? = null,
        @Query("shelf") shelf: String? = null,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): ProductsPagedResponse

    @GET("products/shelves")
    suspend fun productShelves(): List<String>

    @GET("products/{id}")
    suspend fun productById(
        @Path("id") id: String,
    ): ProductDto

    @POST("products")
    suspend fun createProduct(
        @Body body: CreateProductRequest,
    ): ProductDto

    @PATCH("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Body body: UpdateProductRequest,
    ): ProductDto

    @DELETE("products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: String,
    ): ProductDto
}
