package com.example.hda1

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

// --- MODELO PARA EL REGISTRO ---
data class RegisterResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: UserData?
)

data class RegisterUserRequest(
    @SerializedName("Nombre")
    val name: String,

    @SerializedName("Correo_Electronico")
    val email: String,

    @SerializedName("Password")
    val pass: String,

    @SerializedName("Rol")
    val rol: String = "usuario",

    @SerializedName("Edad")
    val edad: Int,

    @SerializedName("Localidad")
    val localidad: String
)

// --- MODELO PARA EL LOGIN ---
data class LoginRequest(
    @SerializedName("Correo_Electronico")
    val email: String,

    @SerializedName("Password")
    val pass: String
)

data class LoginResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("usuario")
    val usuario: UserData?
)

data class UserData(
    // Agregamos el ID para poder enlazar las evaluaciones al usuario correcto
    @SerializedName("ID_usuario", alternate = ["id"])
    val id: Int,

    @SerializedName("Nombre")
    val name: String,
    @SerializedName("Correo_Electronico")
    val email: String,
    @SerializedName("Rol")
    val rol: String
)

// --- MODELOS PARA HISTORIAL DE EVALUACIONES ---
data class RespuestaItemRequest(
    @SerializedName("ID_pregunta") val idPregunta: Int,
    @SerializedName("Respuesta") val respuesta: String,
    @SerializedName("Puntaje") val puntaje: Int
)

data class GuardarEvaluacionRequest(
    @SerializedName("ID_usuario") val idUsuario: Int,
    @SerializedName("ID_test") val idTest: Int,
    @SerializedName("respuestas") val respuestas: List<RespuestaItemRequest>
)

data class EvaluacionResponse(
    @SerializedName("message") val message: String,
    @SerializedName("resumen") val resumen: ResumenEvaluacion
)

data class ResumenEvaluacion(
    @SerializedName("id_evaluacion") val idEvaluacion: Int,
    @SerializedName("puntaje") val puntaje: Int,
    @SerializedName("riesgo") val riesgo: String,
    @SerializedName("diagnostico") val diagnostico: String,
    @SerializedName("sugerencia") val sugerencia: String
)

// --- OTROS MODELOS EXISTENTES ---
data class QuestionModel(
    @SerializedName("ID_pregunta") val id: Int,
    @SerializedName("Pregunta", alternate = ["pregunta"]) val pregunta: String? = "Pregunta sin texto",
    @SerializedName("opciones") val opciones: List<String> = emptyList()
)

// --- INTERFAZ ---
interface ApiService {

    // Ruta: HDA2/login
    @POST("HDA2/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    // Ruta: HDA2/usuario
    @POST("HDA2/usuario")
    suspend fun registerUser(@Body request: RegisterUserRequest): Response<RegisterResponse>

    // Endpoint para preguntas
    @GET("preguntas/test/{id}")
    suspend fun getQuestions(@Path("id") testId: Int): Response<List<QuestionModel>>

    @GET("preguntas/test/{id_test}")
    suspend fun getPreguntasPorTest(@Path("id_test") idTest: Int): Response<List<QuestionModel>>

    // Ruta: HDA2/verificar-2fa
    @POST("HDA2/verificar-2fa")
    suspend fun verify2fa(@Body request: Verify2faRequest): Response<Unit>

    // NUEVO ENDPOINT: Envía los resultados del test e inserta el historial en Railway
    @POST("HDA2/Evaluacion")
    suspend fun guardarEvaluacion(@Body request: GuardarEvaluacionRequest): Response<EvaluacionResponse>
}