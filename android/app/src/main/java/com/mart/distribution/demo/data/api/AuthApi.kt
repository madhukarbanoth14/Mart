package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.api.dto.AuthMeDto
import com.mart.distribution.demo.data.api.dto.ForgotPasswordRequest
import com.mart.distribution.demo.data.api.dto.ForgotPasswordResponse
import com.mart.distribution.demo.data.api.dto.LoginRequest
import com.mart.distribution.demo.data.api.dto.LoginResponse
import com.mart.distribution.demo.data.api.dto.RegistrationAreaDto
import com.mart.distribution.demo.data.api.dto.RegistrationGeoResponse
import com.mart.distribution.demo.data.api.dto.ResetPasswordRequest
import com.mart.distribution.demo.data.api.dto.ResetPasswordResponse
import com.mart.distribution.demo.data.api.dto.SelfRegisterRequest
import com.mart.distribution.demo.data.api.dto.SendOtpRequest
import com.mart.distribution.demo.data.api.dto.SendOtpResponse
import com.mart.distribution.demo.data.api.dto.VerifyOtpRequest
import com.mart.distribution.demo.data.api.dto.VerifyOtpResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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

    @POST("auth/otp/send")
    suspend fun sendOtp(
        @Body body: SendOtpRequest,
    ): SendOtpResponse

    @POST("auth/otp/verify")
    suspend fun verifyOtp(
        @Body body: VerifyOtpRequest,
    ): VerifyOtpResponse

    @GET("auth/registration/geo")
    suspend fun registrationGeo(): RegistrationGeoResponse

    @GET("auth/registration/areas")
    suspend fun registrationAreas(
        @Query("state") state: String? = null,
        @Query("district") district: String? = null,
    ): List<RegistrationAreaDto>

    @POST("auth/register/shopkeeper")
    suspend fun registerShopkeeper(
        @Body body: SelfRegisterRequest,
    ): LoginResponse

    @POST("auth/register/dealer")
    suspend fun registerDealer(
        @Body body: SelfRegisterRequest,
    ): LoginResponse
}
