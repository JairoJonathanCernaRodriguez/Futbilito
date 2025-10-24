package com.robertolopezaguilera.futbilito.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.robertolopezaguilera.futbilito.GameActivity
import com.robertolopezaguilera.futbilito.data.GameDatabase
import com.robertolopezaguilera.futbilito.data.Usuario
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModel
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModelFactory
import com.robertolopezaguilera.futbilito.viewmodel.NivelViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppNavigation(
    startDestination: String,
    db: GameDatabase
) {
    val navController = rememberNavController()
    val nivelViewModel = NivelViewModel(db.nivelDao())
    val context = LocalContext.current

    // 🔹 NUEVO: ViewModel para la pantalla principal
    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(db)
    )

    // 🔹 NUEVO: Cargar usuario al iniciar
    LaunchedEffect(Unit) {
        gameViewModel.loadUsuario()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 🔹 NUEVA RUTA: Pantalla principal
        composable("main") {
            val usuario by gameViewModel.usuario.collectAsState()

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

            RegistroUsuarioScreen { nombre ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        db.usuarioDao().insertUsuario(
                            Usuario(id = 1, nombre = nombre, monedas = 0)
                        )
                    }
                    // 🔹 CAMBIO: Navegar a "main" en lugar de "categorias"
                    navController.navigate("main") {
                        popUpTo("registro") { inclusive = true }
                    }
                }
            }
        }

        composable("categorias") {
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
            NivelesScreen(
                viewModel = nivelViewModel,
                categoria = categoria,
                onNivelClick = { nivelId, _ ->
                    val intent = android.content.Intent(context, GameActivity::class.java)
                    intent.putExtra("nivelId", nivelId)
                    context.startActivity(intent)
                }
            )
        }

        // 🔹 NUEVAS RUTAS: Ajustes y Tienda
        composable("ajustes") {
            AjustesScreen(
                onBackClick = { navController.popBackStack() },
                gameViewModel = gameViewModel
            )
        }

//        composable("tienda") {
//            TiendaScreen(
//                onBackClick = { navController.popBackStack() },
//                gameViewModel = gameViewModel
//            )
//        }
    }
}

