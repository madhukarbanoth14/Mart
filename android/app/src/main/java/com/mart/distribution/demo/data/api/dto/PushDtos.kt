package com.mart.distribution.demo.data.api.dto

data class RegisterFcmTokenRequest(
    val token: String,
)

data class SimpleSuccessResponse(
    val success: Boolean? = null,
)
