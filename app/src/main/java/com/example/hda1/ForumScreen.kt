package com.example.hda1

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(navController: NavHostController, email: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val emailLimpio = email.lowercase()

    // Estados del Foro
    var posts by remember { mutableStateOf<List<ForoResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }

    // Estados para nuevo post / edición
    var nuevoTitulo by remember { mutableStateOf("") }
    var nuevoContenido by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }
    var postAEditar by remember { mutableStateOf<ForoResponse?>(null) } // 🌟 Nuevo estado

    var localUserId by remember { mutableStateOf(-1) }

    LaunchedEffect(Unit) {
        try {
            val userResponse = RetrofitClient.instance.obtenerUsuarios()
            if (userResponse.isSuccessful && userResponse.body() != null) {
                val user = userResponse.body()!!.find { it.email.equals(emailLimpio, ignoreCase = true) }
                if (user != null) localUserId = user.id
            }
        } catch (e: Exception) { }

        try {
            val response = RetrofitClient.instance.obtenerForos()
            if (response.isSuccessful && response.body() != null) {
                posts = response.body()!!
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Sin conexión al foro", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comunidad HDA") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    postAEditar = null
                    nuevoTitulo = ""; nuevoContenido = ""
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Post")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(posts) { post ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                // 🌟 Al tocar un post, lo ponemos en modo edición
                                postAEditar = post
                                nuevoTitulo = post.titulo
                                nuevoContenido = post.contenido
                                showDialog = true
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = post.titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(text = post.contenido, fontSize = 14.sp)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Por: ${post.usuario?.name ?: "Anónimo"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text(text = (post.fecha ?: "2026-05-22").take(10), fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false; postAEditar = null },
                    title = { Text(if (postAEditar != null) "Editar publicación" else "Crear publicación") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = nuevoTitulo, onValueChange = { nuevoTitulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = nuevoContenido, onValueChange = { nuevoContenido = it }, label = { Text("Contenido") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                isPosting = true
                                scope.launch {
                                    val idFinal = if (localUserId != -1) localUserId else 1
                                    val request = CrearForoRequest(idFinal, nuevoTitulo, nuevoContenido, "General")

                                    val response = if (postAEditar != null) {
                                        RetrofitClient.instance.editarForo(postAEditar!!.id, request)
                                    } else {
                                        RetrofitClient.instance.crearForo(request)
                                    }

                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Operación exitosa", Toast.LENGTH_SHORT).show()
                                        val reload = RetrofitClient.instance.obtenerForos()
                                        if (reload.isSuccessful) posts = reload.body() ?: emptyList()
                                        showDialog = false
                                    } else {
                                        Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_LONG).show()
                                    }
                                    isPosting = false
                                }
                            }
                        ) { Text(if (isPosting) "..." else "Guardar") }
                    }
                )
            }
        }
    }
}