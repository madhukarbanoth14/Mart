package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.api.dto.ApproveUserResponse
import com.mart.distribution.demo.data.api.dto.CreateDealerRequest
import com.mart.distribution.demo.data.api.dto.CreateDealerResponse
import com.mart.distribution.demo.data.api.dto.CreateEmployeeRequest
import com.mart.distribution.demo.data.api.dto.CreateEmployeeResponse
import com.mart.distribution.demo.data.api.dto.CreateShopkeeperRequest
import com.mart.distribution.demo.data.api.dto.OnboardingDocumentDto
import com.mart.distribution.demo.data.api.dto.PendingCountResponse
import com.mart.distribution.demo.data.api.dto.UpdateUserStatusRequest
import com.mart.distribution.demo.data.api.dto.UserRowDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File

interface UserApi {
    @GET("users")
    suspend fun users(
        @Query("role") role: String? = null,
        @Query("status") status: String? = null,
    ): List<UserRowDto>

    @GET("users/pending-count")
    suspend fun pendingApprovalCount(): PendingCountResponse

    @POST("users/shopkeepers")
    suspend fun createShopkeeper(
        @Body body: CreateShopkeeperRequest,
    ): UserRowDto

    @POST("users/dealers")
    suspend fun createDealer(
        @Body body: CreateDealerRequest,
    ): CreateDealerResponse

    @POST("users/employees")
    suspend fun createEmployee(
        @Body body: CreateEmployeeRequest,
    ): CreateEmployeeResponse

    @Multipart
    @POST("users/{id}/onboarding-documents")
    suspend fun uploadOnboardingDocument(
        @Path("id") id: String,
        @Part("label") label: okhttp3.RequestBody,
        @Part file: MultipartBody.Part,
    ): OnboardingDocumentDto

    @PATCH("users/{id}/approve")
    suspend fun approveUser(
        @Path("id") id: String,
    ): ApproveUserResponse

    @PATCH("users/{id}/reject")
    suspend fun rejectUser(
        @Path("id") id: String,
        @Body body: UpdateUserStatusRequest = UpdateUserStatusRequest(),
    ): UserRowDto

    @PATCH("users/{id}/deactivate")
    suspend fun deactivateUser(
        @Path("id") id: String,
        @Body body: UpdateUserStatusRequest = UpdateUserStatusRequest(),
    ): UserRowDto

    @PATCH("users/{id}/reactivate")
    suspend fun reactivateUser(
        @Path("id") id: String,
    ): UserRowDto
}
