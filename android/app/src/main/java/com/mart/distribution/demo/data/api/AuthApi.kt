package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.api.dto.AuthMeDto
import com.mart.distribution.demo.data.api.dto.ForgotPasswordRequest
import com.mart.distribution.demo.data.api.dto.ForgotPasswordResponse
import com.mart.distribution.demo.data.api.dto.LoginRequest
import com.mart.distribution.demo.data.api.dto.LoginResponse
import com.mart.distribution.demo.data.api.dto.ResetPasswordRequest
import com.mart.distribution.demo.data.api.dto.ResetPasswordResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest,
    ): LoginResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body body: ForgotPasswordRequest,
    ): ForgotPasswordResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body body: ResetPasswordRequest,
    ): ResetPasswordResponse

    @GET("auth/me")
    suspend fun me(): AuthMeDto
}
