package com.example.androidqr.ui.dashboard

import android.os.Parcelable // ¡Importar Parcelable!
import java.util.Date
import kotlinx.serialization.Serializable // Mantén esta si la usas para JSON
import kotlinx.serialization.Contextual // Necesario para serializar/deserializar Date
import kotlinx.parcelize.Parcelize // ¡Importar la anotación Parcelize!

/**
 * Clase de datos que representa un invitado para la interfaz de usuario.
 * Mapea la información relevante de InvitadoResponse para ser mostrada en la lista.
 */
@Parcelize // ¡Añadir esta anotación!
@Serializable // Marca esta clase para que Kotlinx Serialization la maneje (para JSON)
data class Guest(
    val id: Int,
    val name: String,
    val invitationType: String,
    val status: String, // "Activo", "Vencido", "Usado", "Cancelado"
    val qrCode: String, // Código QR del invitado
    @Contextual val fechaVencimiento: Date?, // Usar @Contextual para Date?
    val residenteId: Int
) : Parcelable // ¡Implementar Parcelable!
