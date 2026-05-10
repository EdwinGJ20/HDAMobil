package com.example.hda1

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

// 1. CORRECCIÓN: Agregamos "isActive" a la data class
data class UserEntry(
    val email: String,
    val name: String,
    val phone: String,
    val isActive: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("UserDatabase", Context.MODE_PRIVATE)

    // Estados para estadísticas
    var totalUsers by remember { mutableIntStateOf(0) }
    var activeUsers by remember { mutableIntStateOf(0) }
    var inactiveUsers by remember { mutableIntStateOf(0) }

    var userList by remember { mutableStateOf(listOf<UserEntry>()) }

    // Función para cargar los datos de SharedPreferences
    fun loadData() {
        val allEntries = prefs.all
        val users = mutableListOf<UserEntry>()
        var total = 0
        var active = 0
        var inactive = 0

        // Filtramos por las llaves que terminan en _password para identificar usuarios únicos
        allEntries.keys.filter { it.endsWith("_password") }.forEach { key ->
            val email = key.replace("_password", "")
            val name = prefs.getString("${email}_name", "Usuario") ?: "Usuario"
            val phone = prefs.getString("${email}_phone", "") ?: ""
            // 2. CORRECCIÓN: Leemos el estado activo/inactivo (por defecto true)
            val activeStatus = prefs.getBoolean("${email}_active", true)

            users.add(UserEntry(email, name, phone, activeStatus))

            total++
            if (activeStatus) active++ else inactive++
        }

        userList = users
        totalUsers = total
        activeUsers = active
        inactiveUsers = inactive
    }

    // Cargamos los datos al iniciar la pantalla
    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Control de Usuarios") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // --- BLOQUE DE ESTADÍSTICAS ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard("Total", totalUsers.toString(), MaterialTheme.colorScheme.primary)
                StatCard("Activos", activeUsers.toString(), Color(0xFF4CAF50))
                StatCard("Inactivos", inactiveUsers.toString(), Color(0xFFF44336))
            }

            // --- LISTA DE USUARIOS ---
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(userList) { user ->
                    ListItem(
                        headlineContent = { Text(user.name) },
                        supportingContent = { Text(user.email) },
                        trailingContent = {
                            // Switch para cambiar el estado Activo/Inactivo
                            Switch(
                                checked = user.isActive,
                                onCheckedChange = { nuevoEstado ->
                                    // Guardamos el nuevo estado en la "base de datos" local
                                    prefs.edit().putBoolean("${user.email}_active", nuevoEstado).apply()
                                    // Recargamos la lista y las estadísticas
                                    loadData()
                                }
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, count: String, color: Color) {
    Card(
        modifier = Modifier.width(100.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(count, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
