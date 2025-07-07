package com.example.androidqr.network // O com.example.androidqr.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    // Formato para enviar a la API (ISO 8601 sin milisegundos)
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    // Formato para recibir la fecha sin la parte de la hora (si la API la envía así)
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(apiDateFormat.format(value))
    }

    override fun deserialize(decoder: Decoder): Date {
        val dateString = decoder.decodeString()
        return try {
            // Intenta primero con el formato completo (ISO 8601)
            apiDateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            // Si falla, intenta con el formato de solo fecha
            try {
                simpleDateFormat.parse(dateString) ?: Date()
            } catch (e2: Exception) {
                System.err.println("Error al deserializar la fecha '$dateString': ${e2.message}")
                Date() // Devuelve la fecha actual como fallback
            }
        }
    }
}
