package com.robertolopezaguilera.futbilito.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.robertolopezaguilera.futbilito.SoundManager
import com.robertolopezaguilera.futbilito.data.TiendaItem
import com.robertolopezaguilera.futbilito.R
import com.robertolopezaguilera.futbilito.data.TiendaDao
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModel
import com.robertolopezaguilera.futbilito.viewmodel.TiendaViewModel
import com.robertolopezaguilera.futbilito.viewmodel.TiendaViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TiendaScreen(
    onBackClick: () -> Unit,
    gameViewModel: GameViewModel,
    tiendaDao: TiendaDao
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    val tiendaViewModel: TiendaViewModel = viewModel(
        factory = TiendaViewModelFactory(gameViewModel, tiendaDao)
    )

    val usuario by gameViewModel.usuario.collectAsState()
    val monedas = usuario?.monedas ?: 0

    val itemsFondo by tiendaViewModel.itemsFondo.collectAsState()
    val itemsPelota by tiendaViewModel.itemsPelota.collectAsState()
    val itemsObstaculo by tiendaViewModel.itemsObstaculo.collectAsState()
    val itemsIcono by tiendaViewModel.itemsIcono.collectAsState()

    var showConfirmacionDialog by remember { mutableStateOf<TiendaItem?>(null) }
    var showMensajeCompra by remember { mutableStateOf(false) }
    var mensajeCompra by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Estado para la pestaña seleccionada
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Fondos", "Pelotas", "Obstáculos", "Iconos")

    LaunchedEffect(showConfirmacionDialog) {
        if (showConfirmacionDialog == null) {
            if (showMensajeCompra) {
                delay(2000)
                showMensajeCompra = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1B4A),
                        Color(0xFF172B6F),
                        Color(0xFF233A89)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        soundManager.playSelectSound()
                        onBackClick()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0x40FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Tienda de Personalización",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Monedas
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x60FFFFFF)),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "$monedas",
                            color = Color(0xFFFFD700),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_coin),
                            contentDescription = "Monedas",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Pestañas
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                edgePadding = 0.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 3.dp,
                        color = Color(0xFFFFD700)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            soundManager.playSelectSound()
                            selectedTab = index
                        },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.7f),
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Contenido de la pestaña seleccionada
            // En TiendaScreen, reemplaza esta parte:
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> CategoriaContenido(
                        titulo = "🎨 Fondos de Pantalla",
                        items = itemsFondo,
                        esIcono = false,
                        onItemClick = { item ->
                            soundManager.playSelectSound()
                            if (item.desbloqueado) {
                                tiendaViewModel.seleccionarItem(item)
                            } else {
                                showConfirmacionDialog = item
                            }
                        }
                    )
                    1 -> CategoriaContenido(
                        titulo = "⚽ Colores de Pelota",
                        items = itemsPelota,
                        esIcono = false,
                        onItemClick = { item ->
                            soundManager.playSelectSound()
                            if (item.desbloqueado) {
                                tiendaViewModel.seleccionarItem(item)
                            } else {
                                showConfirmacionDialog = item
                            }
                        }
                    )
                    2 -> CategoriaContenido(
                        titulo = "🧱 Colores de Obstáculos",
                        items = itemsObstaculo,
                        esIcono = false,
                        onItemClick = { item ->
                            soundManager.playSelectSound()
                            if (item.desbloqueado) {
                                tiendaViewModel.seleccionarItem(item)
                            } else {
                                showConfirmacionDialog = item
                            }
                        }
                    )
                    3 -> CategoriaIconos(
                        titulo = "🎯 Iconos de Pelota",
                        items = itemsIcono,
                        onItemClick = { item ->
                            soundManager.playSelectSound()
                            if (item.desbloqueado) {
                                tiendaViewModel.seleccionarItem(item)
                            } else {
                                showConfirmacionDialog = item
                            }
                        },
                        tiendaViewModel = tiendaViewModel
                    )
                }
            }
        }

        // Diálogos y mensajes (mantener igual)
        showConfirmacionDialog?.let { item ->
            DialogoConfirmacionCompra(
                item = item,
                monedasUsuario = monedas,
                onConfirmar = {
                    scope.launch {
                        tiendaViewModel.comprarItem(item)
                        showConfirmacionDialog = null
                        mensajeCompra = "✅ ${item.nombre} comprado exitosamente!"
                        showMensajeCompra = true
                    }
                },
                onCancelar = {
                    showConfirmacionDialog = null
                    soundManager.playSelectSound()
                }
            )
        }

        if (showMensajeCompra) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Snackbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(12.dp)),
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_coin),
                            contentDescription = "Éxito",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = mensajeCompra,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoriaContenido(
    titulo: String,
    items: List<TiendaItem>,
    esIcono: Boolean,
    onItemClick: (TiendaItem) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = titulo,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // REMOVER el verticalScroll del Column padre y dejar solo LazyVerticalGrid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(items) { index, item ->
                ItemTiendaMejorado(
                    item = item,
                    onClick = { onItemClick(item) },
                    esIcono = esIcono
                )
            }
        }
    }
}

@Composable
private fun CategoriaIconos(
    titulo: String,
    items: List<TiendaItem>,
    onItemClick: (TiendaItem) -> Unit,
    tiendaViewModel: TiendaViewModel
) {
    val iconoSimpleSeleccionado = items.any {
        it.imagenResId == R.drawable.ic_ballsimple && it.seleccionado
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = titulo,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (!iconoSimpleSeleccionado) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x40FFA500)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Información",
                        tint = Color(0xFFFFA500),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Selecciona 'Simple' para poder cambiar los colores de la pelota",
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(items) { index, item ->
                ItemIconoMejorado(
                    item = item,
                    onClick = { onItemClick(item) },
                    permiteColores = iconoSimpleSeleccionado && item.imagenResId == R.drawable.ic_ballsimple
                )
            }
        }
    }
}

@Composable
private fun ItemTiendaMejorado(
    item: TiendaItem,
    onClick: () -> Unit,
    esIcono: Boolean = false
) {
    val colorFondo = if (item.desbloqueado) {
        if (item.seleccionado) Color(0xFF4CAF50) else Color(0x30FFFFFF)
    } else {
        Color(0x15FFFFFF)
    }

    val borderColor = if (item.seleccionado) Color(0xFF4CAF50) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (item.seleccionado) 2.dp else 0.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            // CONTENIDO CENTRADO - Icono o Color
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (esIcono && item.imagenResId != null) {
                    Icon(
                        painter = painterResource(id = item.imagenResId),
                        contentDescription = item.nombre,
                        tint = if (item.desbloqueado) Color.White else Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                } else if (!esIcono && item.colorHex != null) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(item.colorHex)))
                            .border(
                                width = 3.dp,
                                color = if (item.desbloqueado) Color.White.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }

            // Información del item
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.nombre,
                    color = if (item.desbloqueado) Color.White else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = if (item.seleccionado) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (!item.desbloqueado) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${item.precio}",
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (item.seleccionado) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Seleccionado",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Seleccionado",
                            color = Color(0xFF4CAF50),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemIconoMejorado(
    item: TiendaItem,
    onClick: () -> Unit,
    permiteColores: Boolean = false
) {
    val colorFondo = if (item.desbloqueado) {
        if (item.seleccionado) Color(0xFF4CAF50) else Color(0x30FFFFFF)
    } else {
        Color(0x15FFFFFF)
    }

    val borderColor = if (item.seleccionado) Color(0xFF4CAF50) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (item.seleccionado) 2.dp else 0.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            // Icono centrado
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (item.imagenResId != null) {
                    Icon(
                        painter = painterResource(id = item.imagenResId),
                        contentDescription = item.nombre,
                        tint = Color.Unspecified, // Mantener colores originales
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Información del icono
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.nombre,
                    color = if (item.desbloqueado) Color.White else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = if (item.seleccionado) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (!item.desbloqueado) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${item.precio}",
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (item.seleccionado) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Seleccionado",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Seleccionado",
                            color = Color(0xFF4CAF50),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Indicador de colores activos
                if (permiteColores && item.imagenResId == R.drawable.ic_ballsimple) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "🎨 Colores activos",
                        color = Color(0xFF4FC3F7),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// 👇 MEJORADO: Diálogo de confirmación con mejor diseño
@Composable
private fun DialogoConfirmacionCompra(
    item: TiendaItem,
    monedasUsuario: Int,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    val puedeComprar = monedasUsuario >= item.precio

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                "Comprar ${item.nombre}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1B4A)
            )
        },
        text = {
            Column {
                Text("¿Estás seguro de que quieres comprar este item?")
                Spacer(Modifier.height(12.dp))

                // Información del item
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (item.colorHex != null) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(item.colorHex)))
                                .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                    } else if (item.imagenResId != null) {
                        Icon(
                            painter = painterResource(id = item.imagenResId),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.nombre, fontWeight = FontWeight.Medium)
                        Text("Precio: ${item.precio} monedas", color = Color.Gray, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Tus monedas: $monedasUsuario", fontWeight = FontWeight.Medium)

                if (!puedeComprar) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "❌ No tienes suficientes monedas",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmar,
                enabled = puedeComprar,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (puedeComprar) Color(0xFF4CAF50) else Color.Gray
                )
            ) {
                Text("Comprar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancelar,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF44336))
            ) {
                Text("Cancelar")
            }
        }
    )
}
