package com.example.androidqr.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.util.Date

@Serializable
data class InvitadoResponse(
    val id: Int,
    val nombre: String,
    val apellidos: String,
    val tipoInvitacion: String, // "Unica", "Recurrente" o "PorFecha"
    @Contextual val fechaValidez: Date?,
    val qrCode: String,
    val residenteId: Int,
    val estadoQr: String // "Activo", "Vencido", "Usado", "Cancelado"
)

@Serializable
// Modelo para la respuesta de cancelación
data class CancelInvitationResponse(
    val message: String
)

@Serializable
// Modelo para el error de API
data class ApiError(
    val message: String?,
    val errors: Map<String, List<String>>? = null
)
