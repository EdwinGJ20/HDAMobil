package com.example.hda1

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, email: String) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("UserDatabase", Context.MODE_PRIVATE)
    val name = prefs.getString("${email}_name", "Usuario") ?: "Usuario"

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val carouselImages = listOf(
        "https://images.unsplash.com/photo-1555252333-9f8e92e65df9?q=80&w=500",
        "https://images.unsplash.com/photo-1531983412531-1f49a365ffed?q=80&w=500",
        "https://images.unsplash.com/photo-1519681393784-d120267933ba?q=80&w=500",
        "https://images.unsplash.com/photo-1515377905703-c4788e51af15?q=80&w=500",
        "https://images.unsplash.com/photo-1494173853739-c21f58b16055?q=80&w=500",
        "https://images.unsplash.com/photo-1505373633572-2d1f5fec7e0c?q=80&w=500",
        "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?q=80&w=500",
        "https://images.unsplash.com/photo-1502086223501-7ea6ecd79368?q=80&w=500"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menú", modifier = Modifier.padding(16.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = true,
                    icon = { Icon(Icons.Default.Home, null) },
                    onClick = { scope.launch { drawerState.close() } }
                )

                NavigationDrawerItem(
                    label = { Text("Recursos y Apoyo") },
                    selected = false,
                    icon = { Icon(Icons.Default.Favorite, null) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("postpartum_info/$email")
                    }
                )

                // --- 1. ACCESO DESDE EL MENÚ LATERAL ---
                NavigationDrawerItem(
                    label = { Text("Mi Diario Personal") },
                    selected = false,
                    icon = { Icon(Icons.Default.EditNote, null) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("journal/$email")
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                    label = { Text("Comunidad HDA") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() } // Cierra el menú deslizable
                        navController.navigate("forum/$email") // Te manda al Foro pasando el correo
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Realizar Test") },
                    selected = false,
                    icon = { Icon(Icons.Default.CheckCircle, null) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("test/$email")
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Mi Perfil") },
                    selected = false,
                    icon = { Icon(Icons.Default.Person, null) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("profile/$email")
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    icon = { Icon(Icons.Default.ExitToApp, null) },
                    onClick = { navController.navigate("login") }
                )
                // Dentro de ModalDrawerSheet en HomeScreen.kt
                NavigationDrawerItem(
                    label = { Text("Asistente Virtual") },
                    selected = false,
                    icon = { Icon(Icons.Default.SmartToy, null) }, // Necesitas importar Icons.Default.SmartToy
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("chatbot")
                    }
                )

            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bienvenida") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¡Hola, $name!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "Tu bienestar es nuestra prioridad",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                val pagerState = rememberPagerState(pageCount = { carouselImages.size })

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp),
                        pageSpacing = 16.dp
                    ) { page ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            AsyncImage(
                                model = carouselImages[page],
                                contentDescription = "Imagen informativa",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Row(
                        Modifier
                            .height(30.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(carouselImages.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(color)
                                    .size(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de Información
                Button(
                    onClick = { navController.navigate("postpartum_info/$email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Conocer más sobre salud mental")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- 2. ACCESO DESDE BOTÓN PRINCIPAL (DIARIO) ---
                Button(
                    onClick = { navController.navigate("journal/$email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Icon(Icons.Default.Book, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Mi Diario Personal")
                }
            }
        }
    }
}