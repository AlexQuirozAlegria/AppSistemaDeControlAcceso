package com.example.androidqr.network

import retrofit2.Response // Import Retrofit's Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiServiceBD {
    // Define your API endpoint path
//    @POST("your/api/endpoint/generate-qr") // Replace with your actual endpoint
//    suspend fun generateQrCodeFromApi(@Body request: QrDataRequest): Response<QrApiResponse>
    // If your API returns an image URL directly:
    // suspend fun generateQrCodeFromApi(@Body request: QrDataRequest): Response<QrImageResponse>
    @POST("api/Auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

}