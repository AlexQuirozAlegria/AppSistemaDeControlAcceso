package com.example.androidqr.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class LoginResponse(
    @SerialName("token") val token: String?,
    @SerialName("username") val username: String?,
    @SerialName("rol") val rol: String?,
    @SerialName("residenteId") val idResidente: Int?
)
