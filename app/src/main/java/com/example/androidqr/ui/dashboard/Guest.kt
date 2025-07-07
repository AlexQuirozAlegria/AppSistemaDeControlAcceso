package com.example.androidqr.ui.dashboard

import java.util.Date

data class Guest(
    val id: Int, // Or Int, depending on your API
    val name: String,
    val status: String, // This could be "Activo", "Pendiente", "Vencido"
    val fechaVencimiento: Date? // New property for expiration date
    // Add other relevant guest properties
)