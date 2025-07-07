package com.example.androidqr.ui.dashboard

import java.util.Date
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual // Necesario para serializar/deserializar Date

/**
 * Clase de datos que representa un invitado para la interfaz de usuario.
 * Mapea la información relevante de InvitadoResponse para ser mostrada en la lista.
 */
@Serializable // Marca esta clase para que Kotlinx Serialization la maneje
data class Guest(
    val id: Int,
    val name: String,
    val invitationType: String,
    val status: String, // "Activo", "Vencido", "Usado", "Cancelado"
    val qrCode: String, // Código QR del invitado
    @Contextual val fechaVencimiento: Date?, // Usar @Contextual para Date?
    val residenteId: Int
)


