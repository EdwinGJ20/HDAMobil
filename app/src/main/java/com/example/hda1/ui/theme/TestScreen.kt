package com.example.hda1

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
fun TestScreen(navController: NavHostController, email: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // PASO 1: Seguridad - Usamos preferencias encriptadas
    val prefs = SecurityUtils.getSecurePrefs(context)

    // Obtenemos el ID del test que se asignó aleatoriamente en el Login
    val assignedTestId = prefs.getInt("${email}_assigned_test", 1)

    // Estados para la API
    var preguntasApi by remember { mutableStateOf<List<QuestionModel>>(emptyList()) }
    var isLoadingApi by remember { mutableStateOf(true) }

    // Estados del Test
    var respuestas by remember { mutableStateOf(mutableListOf<Int>()) }
    var testFinalizado by remember { mutableStateOf(false) }
    var scoreFinal by remember { mutableStateOf(0) }

    // CARGA DE DATOS DESDE RAILWAY CON REGISTRO DE ERRORES REALES
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.instance.getQuestions(assignedTestId)
            if (response.isSuccessful) {
                val lista = response.body() ?: emptyList()
                preguntasApi = lista
                // Inicializamos la lista de respuestas con -1 según el tamaño que devuelva la API
                respuestas = MutableList(lista.size) { -1 }
            } else {
                // CAPTURAMOS EL ERROR EXACTO DEL SERVIDOR (404, 500, etc.)
                val codigoError = response.code()
                val cuerpoError = response.errorBody()?.string() ?: "Sin mensaje detallado"
                Toast.makeText(context, "Error Servidor ($codigoError): $cuerpoError", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            // CAPTURAMOS SI EL PROBLEMA ES DE PARSEO JSON O DE RED
            Toast.makeText(context, "Falla en Android (Mapeo/Red): ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        } finally {
            isLoadingApi = false
        }
    }

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
        if (isLoadingApi) {
            // PASO 2: Usabilidad - Indicador de carga mientras responde Railway
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
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
                        "Cuestionario asignado #${assignedTestId}. Responde según te has sentido esta última semana.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    preguntasApi.forEachIndexed { index, item ->
                        QuestionItem(
                            index = index,
                            // CORRECCIÓN EXTRA SEGURO: Usamos el operador elvis ?: para pasar un string no nulo
                            pregunta = item.pregunta ?: "Pregunta sin texto disponible",
                            opciones = item.opciones, // Usamos las opciones que vienen de la API
                            selectedOption = respuestas[index],
                            onOptionSelected = { valor ->
                                val newRespuestas = respuestas.toMutableList()
                                newRespuestas[index] = valor
                                respuestas = newRespuestas.toMutableList()
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Button(
                        onClick = {
                            scoreFinal = respuestas.sum()
                            testFinalizado = true
                            // Guardamos el puntaje en la base de datos segura
                            prefs.edit().putInt("${email}_test_score", scoreFinal).apply()
                        },
                        enabled = !respuestas.contains(-1) && preguntasApi.isNotEmpty(),
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
                    Spacer(modifier = Modifier.height(40.dp))

                } else {
                    // --- PANTALLA DE RESULTADOS (Clasificación Estadística) ---
                    val maxPossibleScore = preguntasApi.size * 3
                    val nivel = when {
                        scoreFinal >= (maxPossibleScore * 0.8) -> "Sana / Riesgo Bajo"
                        scoreFinal >= (maxPossibleScore * 0.5) -> "Estado Intermedio"
                        else -> "Estado Delicado"
                    }
                    val colorNivel = when {
                        scoreFinal >= (maxPossibleScore * 0.8) -> Color(0xFF4CAF50)
                        scoreFinal >= (maxPossibleScore * 0.5) -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }

                    Text("Tu Resultado", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Puntaje Total: $scoreFinal / $maxPossibleScore", fontSize = 20.sp)

                    LinearProgressIndicator(
                        progress = { (scoreFinal.toFloat() / maxPossibleScore.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(12.dp).padding(vertical = 4.dp),
                        color = colorNivel,
                        trackColor = colorNivel.copy(alpha = 0.2f)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = colorNivel.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Clasificación:", fontWeight = FontWeight.Bold)
                            Text(nivel, fontSize = 22.sp, color = colorNivel, fontWeight = FontWeight.ExtraBold)
                        }
                    }

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
}

@Composable
fun QuestionItem(index: Int, pregunta: String, opciones: List<String>, selectedOption: Int, onOptionSelected: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${index + 1}. $pregunta", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            opciones.forEachIndexed { optIndex, label ->
                val valorOpcion = (opciones.size - 1) - optIndex
                OptionRow(
                    label = label,
                    isSelected = selectedOption == valorOpcion,
                    onClick = { onOptionSelected(valorOpcion) }
                )
            }
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