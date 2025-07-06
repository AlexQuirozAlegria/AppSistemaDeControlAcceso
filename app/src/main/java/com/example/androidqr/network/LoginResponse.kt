package com.example.androidqr.network

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    // If JSON key is "auth_token", use @SerialName
    // @SerialName("auth_token")
    val token: String,
    val username: String,
    val rol: String
)

@Serializable
data class ApiErrorResponse(
    val errorCode: Int,
    val errorMessage: String
)