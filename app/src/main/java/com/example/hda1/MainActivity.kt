package com.example.hda1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hda1.ui.theme.HDA1Theme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HDA1Theme {
                // Estructura principal de la aplicación con soporte para bordes de pantalla
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // El NavHost gestiona el cambio entre las ventanas del proyecto
    NavHost(navController = navController, startDestination = "login") {

        // 1. Login
        composable("login") {
            LoginScreen(navController)
        }

        // 2. Registro
        composable("register") {
            RegisterScreen(navController)
        }

        // 3. Panel de Administrador
        composable("admin_panel") {
            AdminScreen(navController)
        }

        // 4. Información sobre Depresión Posparto
        composable("postpartum_info/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            PostpartumInfoScreen(navController, email)
        }

        // 5. Perfil de Usuario
        composable("profile/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ProfileScreen(navController, email)
        }

        // 6. Test de Salud Mental
        composable("test/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            TestScreen(navController, email)
        }

        // 7. Inicio (Bienvenida)
        composable("home/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            HomeScreen(navController, email)
        }

        // 8. Diario Personal
        composable("journal/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            JournalScreen(navController, email)
        }

        // 9. Asistente Virtual (ChatBot IA)
        composable("chatbot") {
            ChatBotScreen(navController)
        }
    }
}