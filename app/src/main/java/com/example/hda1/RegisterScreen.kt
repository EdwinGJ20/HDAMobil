package com.example.hda1

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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

@Composable
fun RegisterScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    var passError by remember { mutableStateOf("") }

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
            value = phone,
            onValueChange = { phone = it },
            label = "Teléfono",
            icon = Icons.Default.Phone
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
                if (name.isEmpty()) nameError = true
                if (password != confirmPassword) passError = "Las contraseñas no coinciden"
                if (email.isEmpty()) emailError = "El correo es obligatorio"

                if (!nameError && emailError.isEmpty() && passError.isEmpty()) {
                    val prefs = context.getSharedPreferences("UserDatabase", Context.MODE_PRIVATE)

                    with(prefs.edit()) {
                        putString("${email}_name", name)
                        putString("${email}_phone", phone)
                        putString("${email}_password", password)
                        putBoolean("${email}_active", true)
                        putInt("${email}_visits", 0)
                        apply()
                    }
                    Toast.makeText(context, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = acceptedTerms
        ) {
            Text("Registrarse", fontSize = 18.sp)
        }
    }
}

// ESTA ES LA FUNCIÓN QUE FALTABA COMPLETAR
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