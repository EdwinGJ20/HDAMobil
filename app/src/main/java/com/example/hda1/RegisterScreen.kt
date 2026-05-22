package com.example.hda1

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Estados de los campos
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") } // Conservado por si lo usas localmente
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var edadStr by remember { mutableStateOf("") } // Lo capturamos como texto primero
    var localidad by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }

    // Estados de errores locales
    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    var passError by remember { mutableStateOf("") }
    var edadError by remember { mutableStateOf("") }
    var localidadError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Crear Cuenta", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        Text(text = "Completa tus datos para el registro", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        CustomInput(
            value = name,
            onValueChange = { name = it; nameError = false },
            label = "Nombre Completo",
            icon = Icons.Default.Person,
            isError = nameError,
            errorMessage = if (nameError) "El nombre es obligatorio" else ""
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomInput(
            value = email,
            onValueChange = { email = it; emailError = "" },
            label = "Correo Electrónico",
            icon = Icons.Default.Email,
            isError = emailError.isNotEmpty(),
            errorMessage = emailError
        )

        Spacer(modifier = Modifier.height(12.dp))

        // NUEVO CAMPO: EDAD
        CustomInput(
            value = edadStr,
            onValueChange = { edadStr = it; edadError = "" },
            label = "Edad",
            icon = Icons.Default.DateRange, // Icono de calendario/rango
            isError = edadError.isNotEmpty(),
            errorMessage = edadError
        )

        Spacer(modifier = Modifier.height(12.dp))

        // NUEVO CAMPO: LOCALIDAD
        CustomInput(
            value = localidad,
            onValueChange = { localidad = it; localidadError = false },
            label = "Localidad / Ciudad",
            icon = Icons.Default.LocationOn,
            isError = localidadError,
            errorMessage = if (localidadError) "La localidad es obligatoria" else ""
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomInput(
            value = password,
            onValueChange = { password = it; passError = "" },
            label = "Contraseña",
            icon = Icons.Default.Lock,
            isPassword = true,
            isError = passError.isNotEmpty(),
            errorMessage = passError
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomInput(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; passError = "" },
            label = "Confirmar Contraseña",
            icon = Icons.Default.Lock,
            isPassword = true,
            isError = passError == "Las contraseñas no coinciden"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = acceptedTerms,
                onCheckedChange = { acceptedTerms = it }
            )
            Text(
                text = "Acepto la política de privacidad y protección de datos",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // 1. Validaciones previas locales
                nameError = name.isEmpty()
                localidadError = localidad.isEmpty()

                if (password != confirmPassword) {
                    passError = "Las contraseñas no coinciden"
                } else if (password.isEmpty()) {
                    passError = "La contraseña es obligatoria"
                } else {
                    passError = ""
                }

                if (email.isEmpty()) emailError = "El correo es obligatorio"

                // Validar que la edad sea un número válido
                val edadInt = edadStr.toIntOrNull()
                if (edadStr.isEmpty()) {
                    edadError = "La edad es obligatoria"
                } else if (edadInt == null || edadInt <= 0) {
                    edadError = "Introduce una edad válida"
                }

                // 2. Si todo es correcto, disparamos a Railway
                if (!nameError && emailError.isEmpty() && passError.isEmpty() && edadError.isEmpty() && !localidadError) {
                    isLoading = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.instance.registerUser(
                                RegisterUserRequest(
                                    name = name,
                                    email = email,
                                    pass = password,
                                    edad = edadInt!!, // Seguro de usar porque ya se validó arriba
                                    localidad = localidad
                                )
                            )

                            if (response.isSuccessful) {
                                val prefs = SecurityUtils.getSecurePrefs(context)
                                with(prefs.edit()) {
                                    putString("${email}_name", name)
                                    putBoolean("${email}_active", true)
                                    putInt("${email}_visits", 0)
                                    apply()
                                }

                                Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                val errorMsg = response.errorBody()?.string() ?: "Error en los datos"
                                val cleanErrorMsg = if (response.code() == 404) "Ruta no encontrada (404)" else errorMsg
                                Toast.makeText(context, cleanErrorMsg, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de red: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = acceptedTerms && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Registrarse", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun CustomInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null) },
            isError = isError,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}