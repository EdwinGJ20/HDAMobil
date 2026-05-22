package com.example.hda1

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Hola de nuevo!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Inicia sesión para continuar",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        CustomInput(
            value = email,
            onValueChange = { email = it; emailError = false },
            label = "Correo electrónico",
            icon = Icons.Default.Email,
            isError = emailError,
            errorMessage = if (emailError) "Revisa tus credenciales" else ""
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomInput(
            value = password,
            onValueChange = { password = it; passwordError = false },
            label = "Contraseña",
            icon = Icons.Default.Lock,
            isPassword = true,
            isError = passwordError,
            errorMessage = if (passwordError) "Contraseña incorrecta" else ""
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    isLoading = true

                    scope.launch {
                        try {
                            val response = RetrofitClient.instance.loginUser(
                                LoginRequest(email, password)
                            )

                            if (response.isSuccessful && response.body() != null) {
                                val loginBody = response.body()
                                val usuarioData = loginBody?.usuario

                                if (usuarioData != null) {
                                    val prefs = SecurityUtils.getSecurePrefs(context)

                                    // 🚀 PASO MAESTRO: Guardamos el ID real del usuario que viene de Railway
                                    // para que TestScreen sepa a quién pertenece la evaluación.
                                    prefs.edit().putInt("id_usuario", usuarioData.id).apply()

                                    // 1. EVALUACIÓN DE ROL DINÁMICO DESDE LA BASE DE DATOS
                                    if (usuarioData.rol.equals("admin", ignoreCase = true) ||
                                        usuarioData.rol.equals("administrador", ignoreCase = true)){

                                        val nombreAdmin = usuarioData.name ?: "Principal"
                                        Toast.makeText(context, "¡Bienvenido Administrador: $nombreAdmin!", Toast.LENGTH_SHORT).show()

                                        navController.navigate("admin_panel") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        // 2. FLUJO USUARIO NORMAL CON REDIRECCIÓN A 2FA

                                        // Si el usuario ya tiene un avance registrado (ej: el Test 2 asignado), lo respetamos.
                                        // Si no tiene nada (es 0), le asignamos el Test #1 para arrancar su historial de forma ordenada.
                                        val assignedTest = prefs.getInt("${email}_assigned_test", 0)
                                        if (assignedTest == 0) {
                                            prefs.edit().putInt("${email}_assigned_test", 1).apply()
                                        }

                                        // Verificación de estado de cuenta activo
                                        val isActive = prefs.getBoolean("${email}_active", true)

                                        if (isActive) {
                                            Toast.makeText(context, "Código de verificación enviado", Toast.LENGTH_SHORT).show()
                                            navController.navigate("verification_2fa/$email") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            Toast.makeText(context, "Cuenta inactiva. Contacta al admin.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            } else {
                                val errorServerBody = response.errorBody()?.string()

                                if (response.code() == 401) {
                                    Toast.makeText(context, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Servidor dice: $errorServerBody", Toast.LENGTH_LONG).show()
                                }

                                emailError = true
                                passwordError = true
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de red: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    if (email.isEmpty()) emailError = true
                    if (password.isEmpty()) passwordError = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Iniciar Sesión", fontSize = 18.sp)
            }
        }

        TextButton(onClick = { navController.navigate("register") }) {
            Text("¿No tienes cuenta? ", color = Color.Gray)
            Text("Regístrate aquí", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}