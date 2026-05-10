package com.example.hda1

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Si usas EMULADOR, usa10.0.2.2 para referirte a la PC local.
    // Si usas CELULAR REAL, usa la IP de tu PC (ej: 192.168.1.15).
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}