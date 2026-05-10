package com.example.hda1

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Modelo de lo que envías (ajusta según tu API local)
data class LoginRequest(    val email: String,
                            val pass: String
)

// Modelo de lo que recibes de tu API
data class LoginResponse(
    val success: Boolean,
    val name: String?,
    val message: String?
)

// Interfaz para definir las rutas
interface ApiService {
    @POST("login") // La ruta exacta de tu API (ej: http://localhost:3000/login)
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>
}