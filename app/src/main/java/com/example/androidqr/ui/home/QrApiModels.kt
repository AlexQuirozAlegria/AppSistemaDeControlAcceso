package com.example.androidqr.ui.home

import com.google.gson.annotations.SerializedName

// Data class for the request body
data class QrDataRequest(
    @SerializedName("text2") // Ensure these names match your API's expected JSON keys
    val text2: String,
    @SerializedName("text3")
    val text3: String,
    @SerializedName("invitationType")
    val invitationType: String,
    @SerializedName("date")
    val date: String // Format: "YYYY-MM-DD"
)

// Data class for the API response
data class QrApiResponse(
    @SerializedName("qrData") // Example: if API returns {"qrData": "text_for_qr"}
    val qrData: String?,
    @SerializedName("errorMessage")
    val errorMessage: String?
)
