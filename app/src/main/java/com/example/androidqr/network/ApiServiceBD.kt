package com.example.androidqr.network

import retrofit2.Response // Import Retrofit's Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiServiceBD {
    // Define your API endpoint path
    @POST("api/Invitado/create")
    suspend fun generateQrCode(@Header("Authorization") token: String, @Body request: QrDataRequest): Response<QrDataResponse>

    @GET("api/Invitado/my-invitations")
    suspend fun getMyInvitations(@Header("Authorization") token: String): Response<List<InvitadoResponse>>
    @PUT("api/Invitado/cancel/{id}")
    suspend fun cancelInvitation(@Header("Authorization") token: String, @Path("id") invitationId: Int): Response<CancelInvitationResponse>
    // If your API returns an image URL directly:
    // suspend fun generateQrCodeFromApi(@Body request: QrDataRequest): Response<QrImageResponse>
    @POST("api/Account/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}