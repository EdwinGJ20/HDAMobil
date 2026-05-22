package com.example.hda1

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
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
import kotlin.collections.emptyList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(navController: NavHostController, email: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Recuperamos el contenedor de preferencias encriptadas
    val prefs = SecurityUtils.getSecurePrefs(context)
    val idUsuarioLogueado = prefs.getInt("id_usuario", 1)
    val assignedTestId = prefs.getInt("${email}_assigned_test", 1)

    // Estados para la carga de preguntas
    var preguntasApi by remember { mutableStateOf<List<QuestionModel>>(emptyList()) }
    var isLoadingApi by remember { mutableStateOf(true) }
    var isSendingTest by remember { mutableStateOf(false) }

    // Estados de respuestas del Test
    var respuestas by remember { mutableStateOf(mutableListOf<Int>()) }
    var testFinalizado by remember { mutableStateOf(false) }

    // 🚀 NUEVOS ESTADOS: Almacenan la respuesta real devuelta por el algoritmo de Laravel
    var puntajeFinalServer by remember { mutableIntStateOf(0) }
    var riesgoDetectadoServer by remember { mutableStateOf("") }
    var diagnosticoServer by remember { mutableStateOf("") }
    var sugerenciaServer by remember { mutableStateOf("") }

    // Carga inicial de preguntas desde Railway
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.instance.getQuestions(assignedTestId)
            if (response.isSuccessful) {
                val lista = response.body() ?: emptyList<QuestionModel>()
                preguntasApi = lista
                respuestas = MutableList(lista.size) { -1 }
            } else {
                Toast.makeText(context, "Error Servidor (${response.code()}): No se cargaron los reactivos.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Falla de red en Android: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
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
                    // --- MODO RESOLVER CUESTIONARIO ---
                    Text(
                        text = "Cuestionario asignado #${assignedTestId}. Responde de forma honesta según te has sentido esta última semana.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    preguntasApi.forEachIndexed { index, item ->
                        QuestionItem(
                            index = index,
                            pregunta = item.pregunta ?: "Pregunta sin texto disponible",
                            opciones = item.opciones,
                            selectedOption = respuestas[index],
                            onOptionSelected = { valor ->
                                val newRespuestas = respuestas.toMutableList()
                                newRespuestas[index] = valor
                                respuestas = newRespuestas
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isSendingTest = true

                            // 🚀 CONSTRUCCIÓN DEL REQUEST TRANSACCIONAL PARA LA API
                            val listaItemsEnvio = preguntasApi.mapIndexed { i, item ->
                                RespuestaItemRequest(
                                    idPregunta = item.id,
                                    respuesta = item.opciones.getOrNull((item.opciones.size - 1) - respuestas[i]) ?: "A veces",
                                    puntaje = respuestas[i]
                                )
                            }

                            val requestFinal = GuardarEvaluacionRequest(
                                idUsuario = idUsuarioLogueado,
                                idTest = assignedTestId,
                                respuestas = listaItemsEnvio
                            )

                            // Disparamos la petición HTTP por corrutinas hacia Laravel
                            scope.launch {
                                try {
                                    val response = RetrofitClient.instance.guardarEvaluacion(requestFinal)
                                    if (response.isSuccessful && response.body() != null) {
                                        val resumen = response.body()!!.resumen

                                        // Mapeamos los resultados calculados por los controladores SQL
                                        puntajeFinalServer = resumen.puntaje
                                        riesgoDetectadoServer = resumen.riesgo
                                        diagnosticoServer = resumen.diagnostico
                                        sugerenciaServer = resumen.sugerencia

                                        // Cambiamos el flujo visual de la pantalla
                                        testFinalizado = true
                                        Toast.makeText(context, "Evaluación guardada con éxito en Railway", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Error al procesar evaluación: ${response.code()}", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Falla de conexión: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isSendingTest = false
                                }
                            }
                        },
                        enabled = !respuestas.contains(-1) && preguntasApi.isNotEmpty() && !isSendingTest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(vertical = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (isSendingTest) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (respuestas.contains(-1)) "Faltan preguntas por responder" else "Finalizar y Procesar en Servidor",
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))

                } else {
                    // --- MODO RESULTADOS REALES DESDE EL BACKEND ---
                    val maxPossibleScore = preguntasApi.size * 3

                    // Asignación de color según el riesgo calculado por tu Laravel
                    val colorRiesgo = when {
                        riesgoDetectadoServer.contains("Bajo", ignoreCase = true) || riesgoDetectadoServer.contains("Sana", ignoreCase = true) -> Color(0xFF4CAF50)
                        riesgoDetectadoServer.contains("Intermedio", ignoreCase = true) -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }

                    Icon(
                        imageVector = Icons.Default.AssignmentTurnedIn,
                        contentDescription = null,
                        tint = colorRiesgo,
                        modifier = Modifier.size(72.dp).padding(bottom = 16.dp)
                    )

                    Text("Evaluación Procesada con Éxito", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Puntaje Total Calculado: $puntajeFinalServer / $maxPossibleScore", fontSize = 18.sp, fontWeight = FontWeight.Medium)

                    LinearProgressIndicator(
                        progress = { (puntajeFinalServer.toFloat() / maxPossibleScore.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(10.dp).padding(vertical = 12.dp),
                        color = colorRiesgo,
                        trackColor = colorRiesgo.copy(alpha = 0.2f)
                    )

                    // Tarjeta Diagnóstica Dinámica
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colorRiesgo.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.Start) {
                            Text("Clasificación de Riesgo:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text(riesgoDetectadoServer, fontSize = 22.sp, color = colorRiesgo, fontWeight = FontWeight.ExtraBold)

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Diagnóstico Sugerido:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text(diagnosticoServer, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Sugerencia de Especialistas:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text(sugerenciaServer, fontSize = 14.sp, lineHeight = 20.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Volver a mi Panel de Control")
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
                // Mantenemos la inversión exacta de puntaje que calculaste
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