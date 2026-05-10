package com.example.hda1

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(navController: NavHostController, email: String) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("UserDatabase", Context.MODE_PRIVATE)

    val preguntas = listOf(
        "¿Se ha sentido capaz de reír y ver el lado amable de las cosas?",
        "¿Ha mirado el futuro con placer?",
        "¿Se ha culpado sin necesidad cuando las cosas salían mal?",
        "¿Se ha sentido nerviosa o preocupada sin motivo?",
        "¿Ha sentido miedo o pánico sin motivo?",
        "¿Las cosas la han oprimido o superado?",
        "¿Se ha sentido tan infeliz que ha tenido dificultad para dormir?",
        "¿Se ha sentido triste o desgraciada?",
        "¿Se ha sentido tan infeliz que ha estado llorando?",
        "¿Ha tenido pensamientos de hacerse daño a sí misma?"
    )

    var respuestas by remember { mutableStateOf(MutableList(10) { -1 }) }
    var testFinalizado by remember { mutableStateOf(false) }
    var scoreFinal by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evaluación de Bienestar", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!testFinalizado) {
                Text(
                    "Por favor, selecciona la opción que mejor describa cómo te has sentido esta última semana.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                preguntas.forEachIndexed { index, pregunta ->
                    QuestionItem(
                        index = index,
                        pregunta = pregunta,
                        selectedOption = respuestas[index],
                        onOptionSelected = { valor ->
                            val newRespuestas = respuestas.toMutableList()
                            newRespuestas[index] = valor
                            respuestas = newRespuestas
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Botón de finalización corregido para ser más visible
                Button(
                    onClick = {
                        scoreFinal = respuestas.sum()
                        testFinalizado = true
                        prefs.edit().putInt("${email}_test_score", scoreFinal).apply()
                    },
                    enabled = !respuestas.contains(-1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        if (respuestas.contains(-1)) "Faltan preguntas" else "Finalizar y Ver Resultados",
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(40.dp)) // Espacio extra para que el scroll permita ver el botón bien

            } else {
                // --- PANTALLA DE RESULTADOS ---
                val nivel = when {
                    scoreFinal <= 10 -> "Sana / Riesgo Bajo"
                    scoreFinal <= 15 -> "Estado Intermedio"
                    else -> "Estado Delicado"
                }
                val colorNivel = when {
                    scoreFinal <= 10 -> Color(0xFF4CAF50)
                    scoreFinal <= 15 -> Color(0xFFFFC107)
                    else -> Color(0xFFF44336)
                }

                Text("Tu Resultado", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))

                Text("Puntaje Total: $scoreFinal / 30", fontSize = 20.sp)

                // Barra de progreso visible
                LinearProgressIndicator(
                    progress = { (scoreFinal / 30f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .padding(vertical = 4.dp),
                    color = colorNivel,
                    trackColor = colorNivel.copy(alpha = 0.2f)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = colorNivel.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Clasificación:", fontWeight = FontWeight.Bold)
                        Text(nivel, fontSize = 22.sp, color = colorNivel, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Text(
                    text = if(scoreFinal <= 15) "Siga monitoreando sus emociones regularmente."
                    else "Le recomendamos encarecidamente contactar a un profesional de salud.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Volver al Inicio")
                }
            }
        }
    }
}

@Composable
fun QuestionItem(index: Int, pregunta: String, selectedOption: Int, onOptionSelected: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${index + 1}. $pregunta", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            // Opciones detalladas
            OptionRow("Sí", isSelected = selectedOption == 3, onClick = { onOptionSelected(3) })
            OptionRow("Casi siempre", isSelected = selectedOption == 2, onClick = { onOptionSelected(2) })
            OptionRow("Tal vez", isSelected = selectedOption == 1, onClick = { onOptionSelected(1) })
            OptionRow("No Nunca", isSelected = selectedOption == 0, onClick = { onOptionSelected(0) })
        }
    }
}

@Composable
fun OptionRow(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            RadioButton(selected = isSelected, onClick = onClick)
            Text(label, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyLarge)
        }
    }
}