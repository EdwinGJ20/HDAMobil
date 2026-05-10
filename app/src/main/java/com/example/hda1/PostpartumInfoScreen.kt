package com.example.hda1

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import com.google.firebase.ai.Chat

@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@Composable
fun PostpartumInfoScreen(navController: NavHostController, email: String) {
    val context = LocalContext.current

    val videoUrl = "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Recursos y Apoyo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
        ) {
            // --- HEADER CON VIDEO ---
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Guía de Bienestar",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Entendiendo el Postparto",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // --- BOTONES DE ACCIÓN RÁPIDA ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.Phone,
                    label = "Ayuda Urgente",
                    containerColor = Color(0xFFFFDAD6),
                    contentColor = Color(0xFF410002),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                        context.startActivity(intent)
                    }
                )
                QuickActionCard(
                    icon = Icons.Default.Book,
                    label = "Escribir en mi Diario",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        navController.navigate("journal/$email")
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), thickness = 0.5.dp)

            // --- SECCIÓN INFORMATIVA MEJORADA ---
            Column(modifier = Modifier.padding(16.dp)) {
                InfoSection(
                    title = "¿Qué es la depresión postparto?",
                    content = "Es una afección clínica seria que afecta a muchas madres tras el parto. No es tu culpa y no significa que seas una mala madre; es un desbalance químico y emocional que tiene solución."
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Lista de Síntomas con diseño limpio
                Text("Señales de alerta", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))

                val sintomas = listOf(
                    "Tristeza persistente la mayor parte del día.",
                    "Falta de interés en el bebé.",
                    "Cambios drásticos en el apetito o sueño.",
                    "Pensamientos intrusivos de miedo."
                )

                sintomas.forEach { sintoma ->
                    Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = sintoma, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- NUEVA SECCIÓN: CONSEJOS DIARIOS ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("💡 Consejo del día", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        Text(
                            "Acepta ayuda de familiares. Dormir 30 minutos extra puede marcar una gran diferencia en tu estado de ánimo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun InfoSection(title: String, content: String) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        color = containerColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = contentColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    }
}