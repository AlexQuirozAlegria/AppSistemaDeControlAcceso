package com.example.androidqr.network
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class QrDataRequest(
    val nombre: String,
    val apellidos: String,
    val tipoInvitacion: String,  // "Unica", "Recurrente" o "PorFecha"
    val fechaValidez: Date? = null  // Opcional dependiendo del tipo de invitación
)
@Serializable
data class QrDataResponse(
    val id: Int,
    val nombre: String,
    val apellidos: String,
    val tipoInvitacion: String,
    val fechaValidez: Date?,
    val qrCode: String,
    val residenteId: Int,
    val estadoQr: String,
    val message: String? = null
)
