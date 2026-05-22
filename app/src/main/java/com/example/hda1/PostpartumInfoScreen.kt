package com.example.hda1

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController

@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@Composable
fun PostpartumInfoScreen(navController: NavHostController, email: String) {
    val context = LocalContext.current

    var recomendaciones by remember { mutableStateOf<Recomendaciones?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val videoUrl = "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
        }
    }

    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val userResponse = RetrofitClient.instance.obtenerUsuarios()
            val user = userResponse.body()?.find { it.email.equals(email, ignoreCase = true) }

            if (user != null) {
                val response = RetrofitClient.instance.getPerfilCompleto(user.id)
                if (response.isSuccessful) {
                    recomendaciones = response.body()?.salud?.recomendaciones
                } else {
                    android.util.Log.e("API_ERROR", "Error: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("API_ERROR", "Error: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Recursos y Apoyo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            // --- HEADER Y VIDEO ---
            Text("Guía de Bienestar", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Card(modifier = Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 16.dp), shape = RoundedCornerShape(24.dp)) {
                AndroidView(factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer } }, modifier = Modifier.fillMaxSize())
            }

            // --- ACCIONES ---
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard("Ayuda Urgente", Icons.Default.Phone, Color(0xFFFFDAD6), Color(0xFF410002), Modifier.weight(1f)) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:911")))
                }
                QuickActionCard("Diario", Icons.Default.Book, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, Modifier.weight(1f)) {
                    navController.navigate("journal/$email")
                }
            }

            // --- RECOMENDACIONES DINÁMICAS Y CONTENIDO ---
            Column(modifier = Modifier.padding(16.dp)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (recomendaciones != null) {
                    Text("🌟 Recomendaciones Personalizadas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🍎 Alimentos:", fontWeight = FontWeight.Bold)
                            recomendaciones?.alimentos?.forEach { Text("• ${it.nombre} - ${it.beneficio}") }
                        }
                    }
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🏃 Actividades:", fontWeight = FontWeight.Bold)
                            recomendaciones?.actividades?.forEach { Text("• ${it.nombre}: ${it.descripcion}") }
                        }
                    }
                } else {
                    Text("Completa tu test para recibir recomendaciones personalizadas.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- CONTENIDO ESTÁTICO ORIGINAL ---
                InfoSection("¿Qué es la depresión postparto?", "Es una afección clínica seria que puede afectar a muchas madres después del nacimiento.")

                Spacer(modifier = Modifier.height(16.dp))

                Text("Señales de alerta", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                listOf("Tristeza persistente", "Falta de interés", "Cambios de sueño", "Pensamientos de miedo").forEach {
                    Text("✓ $it", modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(label: String, icon: ImageVector, containerColor: Color, contentColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(100.dp), color = containerColor, shape = RoundedCornerShape(20.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = contentColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, color = contentColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoSection(title: String, content: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    Text(content, style = MaterialTheme.typography.bodyLarge)
}