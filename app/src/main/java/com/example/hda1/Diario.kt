package com.example.hda1

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Diario(navController: NavHostController, email: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val emailLimpio = email.lowercase()

    var noteText by remember { mutableStateOf("") }
    // 🌟 Cambiado a DiarioResponse para coincidir con la estructura de tu BD
    var journalEntries by remember { mutableStateOf<List<DiarioResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isPosting by remember { mutableStateOf(false) }
    var localUserId by remember { mutableStateOf(-1) }

    LaunchedEffect(Unit) {
        try {
            val userResponse = RetrofitClient.instance.obtenerUsuarios()
            if (userResponse.isSuccessful && userResponse.body() != null) {
                val user = userResponse.body()!!.find { it.email.equals(emailLimpio, ignoreCase = true) }
                if (user != null) {
                    localUserId = user.id
                    val response = RetrofitClient.instance.getMisDiarios(localUserId)
                    if (response.isSuccessful && response.body() != null) {
                        journalEntries = response.body()!!
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Diario Personal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, modifier = Modifier.imePadding()) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text("Escribe tu entrada aquí...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (noteText.isNotBlank() && localUserId != -1) {
                                isPosting = true
                                scope.launch {
                                    try {
                                        // 🌟 Enviamos campos completos según tu BD
                                        val request = DiarioRequest(localUserId, "Entrada del día", noteText, "Normal")
                                        val response = RetrofitClient.instance.guardarDiario(request)

                                        if (response.isSuccessful) {
                                            Toast.makeText(context, "Nota guardada", Toast.LENGTH_SHORT).show()
                                            val reload = RetrofitClient.instance.getMisDiarios(localUserId)
                                            if (reload.isSuccessful) journalEntries = reload.body() ?: emptyList()
                                            noteText = ""
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isPosting = false
                                    }
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isPosting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Guardar", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else if (journalEntries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Book, contentDescription = null, Modifier.size(100.dp), tint = Color.Gray.copy(alpha = 0.3f))
                    Text("Sin entradas en tu diario", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(journalEntries) { entry ->
                    JournalBubble(entry)
                }
            }
        }
    }
}

@Composable
fun JournalBubble(entry: DiarioResponse) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = entry.titulo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = entry.contenido)
            Text(text = "Ánimo: ${entry.animo}", fontSize = 10.sp, color = Color.Gray)
        }
    }
}