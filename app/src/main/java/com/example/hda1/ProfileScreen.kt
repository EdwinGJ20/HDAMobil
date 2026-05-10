package com.example.hda1

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController, email: String) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("UserDatabase", Context.MODE_PRIVATE)

    // Estados para los datos actuales (lo que se muestra)
    var currentEmail by remember { mutableStateOf(email) }
    var name by remember { mutableStateOf(prefs.getString("${currentEmail}_name", "") ?: "") }
    var phone by remember { mutableStateOf(prefs.getString("${currentEmail}_phone", "") ?: "") }
    var password by remember { mutableStateOf(prefs.getString("${currentEmail}_password", "") ?: "") }
    var visits = prefs.getInt("${currentEmail}_visits", 0)

    // Estados para la edición (lo que se escribe en los cuadros de texto)
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(name) }
    var editPhone by remember { mutableStateOf(phone) }
    var editEmail by remember { mutableStateOf(currentEmail) }
    var editPassword by remember { mutableStateOf(password) }

    // Estado para la imagen (se carga al entrar)
    var imageUri by remember {
        mutableStateOf<Uri?>(prefs.getString("${currentEmail}_image", null)?.let { Uri.parse(it) })
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            // Guardar imagen inmediatamente
            prefs.edit().putString("${currentEmail}_image", uri.toString()).apply()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isEditing) {
                            // LÓGICA PARA GUARDAR CAMBIOS
                            if (editEmail.isNotEmpty() && editName.isNotEmpty()) {
                                val editor = prefs.edit()

                                // Si el correo cambió, debemos migrar los datos a la nueva llave
                                if (editEmail != currentEmail) {
                                    // Copiar datos a la nueva llave
                                    editor.putString("${editEmail}_name", editName)
                                    editor.putString("${editEmail}_phone", editPhone)
                                    editor.putString("${editEmail}_password", editPassword)
                                    editor.putString("${editEmail}_image", imageUri.toString())
                                    editor.putInt("${editEmail}_visits", visits)

                                    // Borrar datos de la llave vieja
                                    editor.remove("${currentEmail}_name")
                                    editor.remove("${currentEmail}_phone")
                                    editor.remove("${currentEmail}_password")
                                    editor.remove("${currentEmail}_image")
                                    editor.remove("${currentEmail}_visits")

                                    currentEmail = editEmail
                                } else {
                                    // Solo actualizar los campos de la llave actual
                                    editor.putString("${currentEmail}_name", editName)
                                    editor.putString("${currentEmail}_phone", editPhone)
                                    editor.putString("${currentEmail}_password", editPassword)
                                }

                                editor.apply()
                                name = editName
                                phone = editPhone
                                password = editPassword
                                isEditing = false
                                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            isEditing = true
                        }
                    }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = "Acción"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto de Perfil
            Box(contentAlignment = Alignment.BottomEnd) {
                Image(
                    painter = if (imageUri != null) rememberAsyncImagePainter(imageUri)
                    else rememberAsyncImagePainter("https://via.placeholder.com/150"),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )

                SmallFloatingActionButton(
                    onClick = { launcher.launch("image/*") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Cambiar", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isEditing) {
                // VISTA DE LECTURA
                Text(text = name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = currentEmail, color = MaterialTheme.colorScheme.secondary)

                Spacer(modifier = Modifier.height(32.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow(label = "Teléfono", value = phone)
                        InfoRow(label = "Visitas", value = "$visits")
                        InfoRow(label = "Contraseña", value = "********")
                    }
                }
            } else {
                // VISTA DE EDICIÓN
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editEmail,
                    onValueChange = { editEmail = it },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editPhone,
                    onValueChange = { editPhone = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editPassword,
                    onValueChange = { editPassword = it },
                    label = { Text("Nueva Contraseña") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { isEditing = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = MaterialTheme.colorScheme.primary)
    }
}