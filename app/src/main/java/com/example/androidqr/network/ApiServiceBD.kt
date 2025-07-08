package com.example.androidqr.network

import retrofit2.Response // Import Retrofit's Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
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
    suspend fun cancelInvitation(
        @Header("Authorization") token: String,
        @Path("id") invitationId: Int
    ): Response<Unit> // CAMBIO: Espera un cuerpo vacío en caso de éxito

    @DELETE("api/Invitado/{id}")
    suspend fun deleteInvitation(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit> // CAMBIO: Espera un cuerpo vacío en caso de éxito

    @PUT("api/Invitado/{id}")
    suspend fun updateInvitation(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body updatedGuestData: QrDataRequest
    ): Response<InvitadoResponse>

    @POST("api/Account/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // NUEVO: Endpoint para obtener el historial de accesos
    @POST("api/Acceso/history")
    suspend fun getAccessHistory(
        @Header("Authorization") token: String,
        @Body request: AccesoHistoryRequest
    ): Response<AccesoHistoryResponse> // O Response<List<AccesoResponse>> si la API devuelve directamente un array
}
