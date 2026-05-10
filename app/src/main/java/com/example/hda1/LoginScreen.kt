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

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "¡Hola de nuevo!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = "Inicia sesión para continuar", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 32.dp))

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
                // PASO 3: Lógica de Seguridad, Estado y Estadísticas
                if (email == "admin@hda1.com" && password == "admin123") {
                    navController.navigate("admin_panel")
                } else {
                    val prefs = context.getSharedPreferences("UserDatabase", Context.MODE_PRIVATE)
                    val storedPass = prefs.getString("${email}_password", null)
                    val isActive = prefs.getBoolean("${email}_active", true)

                    if (storedPass != null && password == storedPass) {
                        if (isActive) {
                            // REGISTRO ESTADÍSTICO: Sumar visita
                            val visits = prefs.getInt("${email}_visits", 0)
                            prefs.edit().putInt("${email}_visits", visits + 1).apply()

                            navController.navigate("home/$email")
                        } else {
                            // Protocolo de seguridad: Bloqueo de cuenta inactiva
                            Toast.makeText(context, "Cuenta inactiva. Contacta al admin.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        emailError = true
                        passwordError = true
                        Toast.makeText(context, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Iniciar Sesión", fontSize = 18.sp)
        }

        TextButton(onClick = { navController.navigate("register") }) {
            Text("¿No tienes cuenta? ", color = Color.Gray)
            Text("Regístrate aquí", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}