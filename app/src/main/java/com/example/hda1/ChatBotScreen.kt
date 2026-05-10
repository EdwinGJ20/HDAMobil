package com.example.hda1

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.vertexai.type.content
import com.google.firebase.vertexai.vertexAI
import kotlinx.coroutines.launch

data class BotMessage(val text: String, val isUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    var userInput by remember { mutableStateOf("") }
    val chatMessages = remember {
        mutableStateListOf(BotMessage("¡Hola! Soy tu asistente HDA1. Estoy aquí para escucharte. ¿Cómo te sientes hoy?", false))
    }
    var isTyping by remember { mutableStateOf(false) }

    // Configuración de la IA con instrucciones de seguridad y empatía
    val model = Firebase.vertexAI.generativeModel(
        modelName = "gemini-1.5-flash",
        systemInstruction = content {
            text("Eres un asistente empático experto en depresión posparto. " +
                    "Si detectas riesgo de daño personal, sugiere ayuda profesional inmediata.")
        }
    )
    val chat = remember { model.startChat() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asistente Virtual IA") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(modifier = Modifier.weight(1f).padding(16.dp)) {
                items(chatMessages) { msg ->
                    ChatBubble(msg)
                }
            }

            Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe aquí...") },
                        shape = RoundedCornerShape(24.dp)
                    )
                    IconButton(onClick = {
                        if (userInput.isNotEmpty()) {
                            val text = userInput
                            chatMessages.add(BotMessage(text, true))
                            userInput = ""
                            isTyping = true
                            scope.launch {
                                try {
                                    val response = chat.sendMessage(text)
                                    chatMessages.add(BotMessage(response.text ?: "No pude procesar eso", false))
                                } catch (e: Exception) {
                                    chatMessages.add(BotMessage("Error de conexión", false))
                                } finally { isTyping = false }
                            }
                        }
                    }) { Icon(Icons.Default.Send, null, tint = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: BotMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val color = if (message.isUser) MaterialTheme.colorScheme.primary else Color(0xFFECECEC)
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = alignment) {
        Surface(color = color, shape = RoundedCornerShape(12.dp)) {
            Text(text = message.text, modifier = Modifier.padding(12.dp), color = if (message.isUser) Color.White else Color.Black)
        }
    }
}