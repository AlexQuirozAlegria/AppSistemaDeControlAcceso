package com.example.androidqr.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.Date
import com.google.gson.JsonDeserializer // Para deserializar
import com.google.gson.JsonSerializer // Para serializar
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import java.lang.reflect.Type // Necesario para el TypeAdapter

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:5295/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log request and response bodies
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // 1. Crea un serializador/deserializador personalizado para Date
    private val dateTypeAdapter = object : JsonSerializer<Date>, JsonDeserializer<Date> {
        override fun serialize(
            src: Date?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            // Si la fecha es nula, serializa como nulo
            return if (src == null) JsonPrimitive("") else JsonPrimitive(src.time)
        }

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Date? {
            return if (json == null || json.asString.isEmpty()) null else Date(json.asLong)
        }
    }

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, dateTypeAdapter)
        .setLenient()
        .create()

    val instance: ApiServiceBD by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        retrofit.create(ApiServiceBD::class.java)
    }
}