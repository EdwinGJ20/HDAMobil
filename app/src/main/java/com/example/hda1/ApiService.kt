package com.example.hda1

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

// --- MODELOS DE RESPUESTA GENERAL ---
data class GeneralResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

data class RegisterResponse(
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UserData?
)

// --- MODELOS DE PETICIÓN ---
data class RegisterUserRequest(
    @SerializedName("Nombre") val name: String,
    @SerializedName("Correo_Electronico") val email: String,
    @SerializedName("Password") val pass: String,
    @SerializedName("Rol") val rol: String = "usuario",
    @SerializedName("Edad") val edad: Int,
    @SerializedName("Localidad") val localidad: String
)

data class LoginRequest(
    @SerializedName("Correo_Electronico") val email: String,
    @SerializedName("Password") val pass: String
)

data class LoginResponse(
    @SerializedName("message") val message: String,
    @SerializedName("usuario") val usuario: UserData?
)

data class UserData(
    @SerializedName("ID_usuario", alternate = ["id"]) val id: Int,
    @SerializedName("Nombre") val name: String,
    @SerializedName("Correo_Electronico") val email: String,
    @SerializedName("Rol") val rol: String
)

data class Verify2faRequest(
    @SerializedName("Correo_Electronico") val email: String,
    @SerializedName("Codigo_2fa") val code: String
)

// --- EVALUACIONES ---
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

data class QuestionModel(
    @SerializedName("ID_pregunta") val id: Int,
    @SerializedName("Pregunta", alternate = ["pregunta"]) val pregunta: String? = "Pregunta sin texto",
    @SerializedName("opciones") val opciones: List<String> = emptyList()
)

// --- FORO ---
data class ForoResponse(
    @SerializedName("ID_foro", alternate = ["id"]) val id: Int,
    @SerializedName("Titulo") val titulo: String,
    @SerializedName("Contenido") val contenido: String,
    @SerializedName("created_at") val fecha: String? = "2026-05-22",
    @SerializedName("ID_usuario") val idUsuario: Int,
    @SerializedName("usuario") val usuario: UserData? = null
)

data class CrearForoRequest(
    @SerializedName("ID_usuario") val idUsuario: Int,
    @SerializedName("Titulo") val titulo: String,
    @SerializedName("Contenido") val contenido: String,
    @SerializedName("Categoria") val categoria: String = "General"
)
// --- MODELOS DE DIARIO ---
data class DiarioResponse(
    @SerializedName("ID_diario") val id: Int,
    @SerializedName("Titulo") val titulo: String,
    @SerializedName("Entrada") val contenido: String,
    @SerializedName("Estado_Animo") val animo: String,
    @SerializedName("created_at") val fecha: String? = "2026-05-22"
)

data class DiarioRequest(
    @SerializedName("ID_usuario") val idUsuario: Int,
    @SerializedName("Titulo") val titulo: String,
    @SerializedName("Entrada") val entrada: String,
    @SerializedName("Estado_Animo") val estadoAnimo: String
)
// Modelos para las recomendaciones
data class AlimentoItem(
    @SerializedName("Nombre") val nombre: String,
    @SerializedName("Beneficio") val beneficio: String,
    @SerializedName("Cantidad_recomendada") val cantidad: String
)

data class ActividadItem(
    @SerializedName("Nombre") val nombre: String,
    @SerializedName("Descripcion") val descripcion: String,
    @SerializedName("Beneficio") val beneficio: String
)

// Asegúrate de tener estos modelos definidos así:

data class PerfilCompletoResponse(
    @SerializedName("salud") val salud: SaludData? // Cambia Any? por SaludData?
)

data class SaludData(
    @SerializedName("recomendaciones") val recomendaciones: Recomendaciones?
)

data class Recomendaciones(
    @SerializedName("alimentos") val alimentos: List<AlimentoItem>?,
    @SerializedName("actividades") val actividades: List<ActividadItem>?
)

// Asegúrate de que AlimentoItem y ActividadItem existan también


// --- INTERFAZ API ---
interface ApiService {

    // --- AUTENTICACIÓN Y USUARIOS ---
    @POST("HDA2/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("HDA2/usuario")
    suspend fun registerUser(@Body request: RegisterUserRequest): Response<RegisterResponse>

    @POST("HDA2/verificar-2fa")
    suspend fun verify2fa(@Body request: Verify2faRequest): Response<Unit>

    @GET("HDA2/usuario")
    suspend fun obtenerUsuarios(): Response<List<UserData>>

    @PUT("HDA2/usuario/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: Int,
        @Body datos: Map<String, String>
    ): Response<RegisterResponse>

    // --- CUESTIONARIOS ---
    @GET("preguntas/test/{id}")
    suspend fun getQuestions(@Path("id") testId: Int): Response<List<QuestionModel>>

    // --- EVALUACIONES ---
    @POST("HDA2/Evaluacion")
    suspend fun guardarEvaluacion(@Body request: GuardarEvaluacionRequest): Response<EvaluacionResponse>

    // --- FORO ---
    @GET("HDA2/foros")
    suspend fun obtenerForos(): Response<List<ForoResponse>>

    @POST("HDA2/foros")
    suspend fun crearForo(@Body request: CrearForoRequest): Response<Map<String, Any>>

    @PUT("HDA2/foros/{id}")
    suspend fun editarForo(
        @Path("id") id: Int,
        @Body request: CrearForoRequest
    ): Response<GeneralResponse>

    // --- AGREGA ESTO A TU INTERFAZ API ---
// --- En tu interface ApiService ---
    @GET("HDA2/diario/{idUsuario}")
    suspend fun getMisDiarios(@Path("idUsuario") idUsuario: Int): Response<List<DiarioResponse>>

    @POST("HDA2/diario")
    suspend fun guardarDiario(@Body request: DiarioRequest): Response<GeneralResponse>

    @GET("HDA2/perfil_completo/{idUsuario}")
    suspend fun getPerfilCompleto(@Path("idUsuario") idUsuario: Int): Response<PerfilCompletoResponse>
}