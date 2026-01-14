package com.robertolopezaguilera.futbilito.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.robertolopezaguilera.futbilito.GameActivity
import com.robertolopezaguilera.futbilito.MusicManager
import com.robertolopezaguilera.futbilito.data.GameDatabase
import com.robertolopezaguilera.futbilito.data.Usuario
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModel
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModelFactory
import com.robertolopezaguilera.futbilito.viewmodel.NivelViewModel
import com.robertolopezaguilera.futbilito.viewmodel.TiendaViewModel
import com.robertolopezaguilera.futbilito.viewmodel.TiendaViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppNavigation(
    startDestination: String,
    db: GameDatabase,
    onGameActivityLaunched: () -> Unit = {}
) {
    val navController = rememberNavController()
    val nivelViewModel = NivelViewModel(db.nivelDao())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 🔹 ViewModel para la pantalla principal
    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(db)
    )

    // 🔹 Cargar usuario al iniciar
    LaunchedEffect(Unit) {
        gameViewModel.loadUsuario()
    }

    // 🔹 MEJORADO: Control centralizado de música para navegación
    var currentDestination by remember { mutableStateOf(startDestination) }

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val previousDestination = currentDestination
            currentDestination = destination.route ?: startDestination

            Log.d("AppNavigation", "📍 Navegación: $previousDestination -> $currentDestination")

            // 🔹 CRÍTICO: Asegurar música MENU cuando navegamos entre pantallas del menú
            if (currentDestination != "game" && currentDestination != previousDestination) {
                scope.launch {
                    delay(50) // Pequeño delay para estabilizar la navegación
                    Log.d("AppNavigation", "🎵 Asegurando música MENU después de navegar a: $currentDestination")
                    MusicManager.ensureMenuMusic(context)
                }
            }
        }
    }

    // 🔹 MEJORADO: Manejo específico del botón de back
    val backHandler = remember { android.widget.Toast.makeText(context, "Presiona nuevamente para salir", android.widget.Toast.LENGTH_SHORT) }

    // Si necesitas manejar el back press de manera específica, puedes usar:
    // BackHandler(enabled = true) { ... }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 🔹 Pantalla principal
        composable("main") {
            val usuario by gameViewModel.usuario.collectAsState()

            // 🔹 MEJORADO: Verificación de música solo cuando sea necesario
            LaunchedEffect(Unit) {
                // Solo verificar música si acabamos de llegar a esta pantalla
                delay(100)
                Log.d("MainScreen", "🔄 Verificando música en pantalla principal")
                MusicManager.ensureMenuMusic(context)
            }

            MainScreen(
                usuario = usuario,
                gameViewModel = gameViewModel,
                onPlayClick = {
                    navController.navigate("categorias")
                },
                onSettingsClick = {
                    navController.navigate("ajustes")
                },
                onShopClick = {
                    navController.navigate("tienda")
                }
            )
        }

        composable("registro") {
            val scope = rememberCoroutineScope()

            // 🔹 MEJORADO: Verificación de música solo cuando sea necesario
            LaunchedEffect(Unit) {
                delay(100)
                Log.d("RegistroScreen", "🔄 Verificando música en registro")
                MusicManager.ensureMenuMusic(context)
            }

            RegistroUsuarioScreen { nombre ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        db.usuarioDao().insertUsuario(
                            Usuario(id = 1, nombre = nombre, monedas = 0)
                        )
                    }
                    navController.navigate("main") {
                        popUpTo("registro") { inclusive = true }
                    }
                }
            }
        }

        composable("categorias") {
            // 🔹 MEJORADO: Verificación de música solo cuando sea necesario
            LaunchedEffect(Unit) {
                delay(100)
                Log.d("CategoriasScreen", "🔄 Verificando música en categorías")
                MusicManager.ensureMenuMusic(context)
            }

            CategoriasScreen(
                viewModel = nivelViewModel,
                onCategoriaClick = { categoria ->
                    navController.navigate("niveles/$categoria")
                }
            )
        }

        composable(
            "niveles/{categoria}",
            arguments = listOf(navArgument("categoria") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoria = backStackEntry.arguments?.getString("categoria") ?: ""

            // 🔹 MEJORADO: Verificación de música solo cuando sea necesario
            LaunchedEffect(Unit) {
                delay(100)
                Log.d("NivelesScreen", "🔄 Verificando música en niveles")
                MusicManager.ensureMenuMusic(context)
            }

            NivelesScreen(
                viewModel = nivelViewModel,
                categoria = categoria,
                onNivelClick = { nivelId, _ ->
                    onGameActivityLaunched()
                    Log.d("AppNavigation", "🚀 Lanzando GameActivity para nivel $nivelId")
                    val intent = android.content.Intent(context, GameActivity::class.java)
                    intent.putExtra("nivelId", nivelId)
                    context.startActivity(intent)
                }
            )
        }

        composable("ajustes") {
            // 🔹 MEJORADO: Verificación de música solo cuando sea necesario
            LaunchedEffect(Unit) {
                delay(100)
                Log.d("AjustesScreen", "🔄 Verificando música en ajustes")
                MusicManager.ensureMenuMusic(context)
            }

            AjustesScreen(
                onBackClick = {
                    Log.d("AjustesScreen", "🔙 Navegando hacia atrás desde ajustes")
                    navController.popBackStack()
                },
                gameViewModel = gameViewModel
            )
        }

        composable("tienda") {
            // 🔹 MEJORADO: Verificación de música solo cuando sea necesario
            LaunchedEffect(Unit) {
                delay(100)
                Log.d("TiendaScreen", "🔄 Verificando música en tienda")
                MusicManager.ensureMenuMusic(context)
            }

            TiendaScreen(
                onBackClick = {
                    Log.d("TiendaScreen", "🔙 Navegando hacia atrás desde tienda")
                    navController.popBackStack()
                },
                gameViewModel = gameViewModel,
                tiendaDao = db.tiendaDao()
            )
        }
    }
}