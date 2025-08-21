package com.robertolopezaguilera.futbilito.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.robertolopezaguilera.futbilito.GameActivity
import com.robertolopezaguilera.futbilito.data.GameDatabase
import com.robertolopezaguilera.futbilito.data.Usuario
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
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("registro") {
            val scope = rememberCoroutineScope()

            RegistroUsuarioScreen { nombre ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        db.usuarioDao().insertUsuario(
                            Usuario(id = 1, nombre = nombre, monedas = 0)
                        )
                    }
                    navController.navigate("categorias") {
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
                    // ✅ Usamos LocalContext en lugar de "it"
                    val intent = android.content.Intent(context, GameActivity::class.java)
                    intent.putExtra("nivelId", nivelId)
                    context.startActivity(intent)
                }
            )
        }
    }
}

