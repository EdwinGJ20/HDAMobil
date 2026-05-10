package com.example.hda1

import android.content.Context
import androidx.compose.foundation.background
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(navController: NavHostController, email: String) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("UserJournal_${email}", Context.MODE_PRIVATE)

    // Estado para el texto que se está escribiendo
    var noteText by remember { mutableStateOf("") }

    // Lista de notas guardadas (cargamos las existentes)
    var journalEntries by remember {
        mutableStateOf(prefs.all.values.map { it.toString() }.sortedDescending())
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
            // Barra inferior para escribir (tipo chat)
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.imePadding() // Evita que el teclado tape el campo
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text("¿Cómo te sientes hoy?") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = {
                            if (noteText.isNotBlank()) {
                                val timeStamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                val newEntry = "[$timeStamp]\n$noteText"

                                // Guardar en SharedPreferences
                                prefs.edit().putString(System.currentTimeMillis().toString(), newEntry).apply()

                                // Actualizar lista visual
                                journalEntries = (journalEntries + newEntry).sortedDescending()
                                noteText = ""
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Guardar", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        if (journalEntries.isEmpty()) {
            // Pantalla vacía
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Book, contentDescription = null, Modifier.size(100.dp), tint = Color.Gray.copy(alpha = 0.3f))
                    Text("Aún no hay entradas en tu diario", color = Color.Gray)
                }
            }
        } else {
            // Lista de entradas tipo burbujas
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(journalEntries) { entry ->
                    JournalBubble(entry)
                }
            }
        }
    }
}

@Composable
fun JournalBubble(content: String) {
    val parts = content.split("\n", limit = 2)
    val date = parts.getOrNull(0) ?: ""
    val text = parts.getOrNull(1) ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp
            )
        }
    }
}