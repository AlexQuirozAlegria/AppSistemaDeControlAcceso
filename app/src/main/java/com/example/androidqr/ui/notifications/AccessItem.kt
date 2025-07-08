package com.example.androidqr.ui.notifications

import java.util.Date

/**
 * Clase de datos para representar un elemento de acceso en la UI.
 */
data class AccessItem(
    val id: Int,
    val fechaAcceso: Date,
    val tipoAcceso: String,
    val nombreInvitado: String?, // Puede ser nulo si es un acceso del residente
    val nombreGuardia: String?,
    val placasVehiculo: String?
)
