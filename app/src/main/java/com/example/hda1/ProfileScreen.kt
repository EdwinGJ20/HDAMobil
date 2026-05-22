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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController, email: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("UserDatabase", Context.MODE_PRIVATE)

    // 🌟 TRUCO ANTIBUGS: Forzamos el correo de entrada a minúsculas
    var currentEmail by remember { mutableStateOf(email.lowercase()) }

    // Estados de Identificación Remota
    var userId by remember { mutableStateOf(-1) }

    // Estados para los datos actuales (apuntando a la llave limpia en minúsculas)
    var name by remember { mutableStateOf(prefs.getString("${currentEmail}_name", "Cargando...") ?: "Cargando...") }
    var phone by remember { mutableStateOf(prefs.getString("${currentEmail}_phone", "") ?: "") }
    var password by remember { mutableStateOf("") }
    var visits by remember { mutableStateOf(prefs.getInt("${currentEmail}_visits", 0)) }

    // Estados para la edición
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Imagen de perfil local
    var imageUri by remember {
        mutableStateOf<Uri?>(prefs.getString("${currentEmail}_image", null)?.let { Uri.parse(it) })
    }

    // 🚀 AL ENTRAR: Sincronizamos con Railway y recargamos visitas frescas
    LaunchedEffect(currentEmail) {
        // Volvemos a leer visitas por si acaso acaba de aumentar en el 2FA
        visits = prefs.getInt("${currentEmail}_visits", 0)

        try {
            val response = RetrofitClient.instance.obtenerUsuarios()
            if (response.isSuccessful && response.body() != null) {
                val usuarioRemoto = response.body()!!.find { it.email.equals(currentEmail, ignoreCase = true) }
                if (usuarioRemoto != null) {
                    userId = usuarioRemoto.id
                    name = usuarioRemoto.name

                    // Respaldo local
                    prefs.edit().putString("${currentEmail}_name", name).apply()
                }
            }
        } catch (e: Exception) {
            // Falla de red, mantiene SharedPreferences
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
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
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(end = 16.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = {
                            if (isEditing) {
                                if (editEmail.isNotEmpty() && editName.isNotEmpty()) {
                                    isLoading = true
                                    scope.launch {
                                        try {
                                            // Correo limpio para guardar
                                            val targetEmail = editEmail.lowercase()

                                            val camposActualizar = mutableMapOf(
                                                "Nombre" to editName,
                                                "Correo_Electronico" to targetEmail
                                            )
                                            if (editPassword.isNotEmpty()) {
                                                camposActualizar["Password"] = editPassword
                                            }

                                            val response = RetrofitClient.instance.actualizarUsuario(userId, camposActualizar)

                                            if (response.isSuccessful) {
                                                val editor = prefs.edit()

                                                if (targetEmail != currentEmail) {
                                                    editor.putString("${targetEmail}_name", editName)
                                                    editor.putString("${targetEmail}_phone", editPhone)
                                                    editor.putString("${targetEmail}_image", imageUri.toString())
                                                    editor.putInt("${targetEmail}_visits", visits)

                                                    editor.remove("${currentEmail}_name")
                                                    editor.remove("${currentEmail}_phone")
                                                    editor.remove("${currentEmail}_image")
                                                    editor.remove("${currentEmail}_visits")

                                                    currentEmail = targetEmail
                                                } else {
                                                    editor.putString("${currentEmail}_name", editName)
                                                    editor.putString("${currentEmail}_phone", editPhone)
                                                }

                                                editor.apply()
                                                name = editName
                                                phone = editPhone
                                                password = ""
                                                isEditing = false
                                                Toast.makeText(context, "¡Perfil guardado en Railway!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Error del servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error de red: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            } else {
                                editName = name
                                editEmail = currentEmail
                                editPhone = phone
                                editPassword = ""
                                isEditing = true
                            }
                        }) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = "Acción"
                            )
                        }
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
                Text(text = name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = currentEmail, color = MaterialTheme.colorScheme.secondary)

                Spacer(modifier = Modifier.height(32.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow(label = "Teléfono local", value = phone)
                        InfoRow(label = "Visitas registradas", value = "$visits")
                        InfoRow(label = "Servidor ID asignado", value = if(userId == -1) "Buscando..." else "#$userId")
                    }
                }
            } else {
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
                    label = { Text("Nueva Contraseña (Dejar vacío para no cambiar)") },
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = MaterialTheme.colorScheme.primary)
    }
}