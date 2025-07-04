package com.example.androidqr.ui.dashboard

data class Guest(
    val id: Int, // Or Int, depending on your API
    val name: String,
    val status: String // This could be "Activo", "Pendiente", "Vencido"
    // Add other relevant guest properties
)