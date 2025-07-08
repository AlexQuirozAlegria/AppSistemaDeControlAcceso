package com.example.androidqr.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.util.Date

// Modelo para la solicitud del historial de accesos
@Serializable
data class AccesoHistoryRequest(
    @Contextual val fechaInicio: Date? = null,
    @Contextual val fechaFin: Date? = null,
    val residenteId: Int? = null, // El ID del residente logueado
    val invitadoId: Int? = null,
    val tipoAcceso: String? = null, // "Entrada", "Salida"
    val guardiaId: Int? = null,
    val placasVehiculo: String? = null
)

// Modelo para la respuesta de un acceso individual en el historial
@Serializable
data class AccesoResponse(
    val id: Int,
    @Contextual val fechaAcceso: Date,
    val residenteId: Int,
    val invitadoId: Int?,
    val tipoAcceso: String,
    val guardiaId: Int?,
    val placasVehiculo: String?,
    val nombreResidente: String?,
    val nombreInvitado: String?,
    val nombreGuardia: String?
)

// Modelo para la respuesta de la lista de accesos
@Serializable
data class AccesoHistoryResponse(
    val accesos: List<AccesoResponse> // Asumiendo que la API devuelve una lista de accesos bajo una clave "accesos"
    // Si la API devuelve directamente un array, esto sería solo `List<AccesoResponse>`
)
