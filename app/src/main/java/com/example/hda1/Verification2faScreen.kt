package com.example.hda1

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import com.google.gson.annotations.SerializedName
@Composable
fun Verification2faScreen(navController: NavHostController, email: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var code by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Verificación de Seguridad", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Hemos enviado un código de 6 dígitos a tu correo: $email", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(32.dp))

        CustomInput(
            value = code,
            onValueChange = { if (it.length <= 6) code = it; isError = false },
            label = "Código de 6 dígitos",
            icon = Icons.Default.Lock,
            isError = isError,
            errorMessage = if (isError) "Código incorrecto o expirado" else ""
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (code.length == 6) {
                    isLoading = true
                    scope.launch {
                        try {
                            // Petición a Laravel para comprobar el código token
                            val response = RetrofitClient.instance.verify2fa(Verify2faRequest(email, code))

                            if (response.isSuccessful) {
                                Toast.makeText(context, "¡Verificación Correcta!", Toast.LENGTH_SHORT).show()

                                // Guardamos las estadísticas locales igual que antes
                                val prefs = SecurityUtils.getSecurePrefs(context)
                                val visits = prefs.getInt("${email}_visits", 0)
                                prefs.edit().putInt("${email}_visits", visits + 1).apply()

                                // Pasamos finalmente al Home completo
                                navController.navigate("home/$email") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                isError = true
                                Toast.makeText(context, "Código inválido", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de red: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    Toast.makeText(context, "El código debe tener 6 dígitos", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading && code.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Verificar Código", fontSize = 18.sp)
            }
        }
    }
}

// Clases de soporte para el Request de verificación
data class Verify2faRequest(
    @SerializedName("Correo_Electronico") val email: String,
    @SerializedName("Codigo_2fa") val code: String
)