package com.robertolopezaguilera.futbilito.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robertolopezaguilera.futbilito.MusicManager
import com.robertolopezaguilera.futbilito.MusicService
import com.robertolopezaguilera.futbilito.SoundManager
import com.robertolopezaguilera.futbilito.data.Usuario
import com.robertolopezaguilera.futbilito.viewmodel.GameViewModel
import com.robertolopezaguilera.futbilito.R

@Composable
fun AjustesScreen(
    onBackClick: () -> Unit,
    gameViewModel: GameViewModel
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    val usuario by gameViewModel.usuario.collectAsState()

    // Estados para los ajustes
    var notificationsEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var showEditNameDialog by remember { mutableStateOf(false) }

    // 🔹 CORRECCIÓN: Volumen de música ahora se maneja con MusicManager
    var musicVolume by remember { mutableStateOf(0.7f) } // Valor por defecto
    var soundVolume by remember { mutableStateOf(soundManager.loadEffectsVolume()) }
    var previousMusicVolume by remember { mutableStateOf(0.7f) }
    var previousSoundVolume by remember { mutableStateOf(soundManager.loadEffectsVolume()) }

    // 🔹 CORRECCIÓN: Las funciones deben retornar Unit, no Int
    val onMusicVolumeChange: (Float) -> Unit = { newVolume: Float ->
        musicVolume = newVolume
        // 🔹 CAMBIO: Usar MusicManager para volumen de música
        MusicManager.setMusicVolume(context, newVolume)
        // Solo reproducir sonido si no es mute completo
        if (newVolume > 0) {
            soundManager.playSelectSound()
        }
        Log.d("AjustesScreen", "Volumen de música cambiado a: ${(newVolume * 100).toInt()}%")
    }

    val onSoundVolumeChange: (Float) -> Unit = { newVolume: Float ->
        soundVolume = newVolume
        // 🔹 CORRECTO: SoundManager maneja solo efectos
        soundManager.setEffectsVolume(newVolume)
        Log.d("AjustesScreen", "Volumen de efectos cambiado a: ${(newVolume * 100).toInt()}%")
    }

    val onMusicMuteToggle: () -> Unit = {
        if (musicVolume > 0f) {
            // Silenciar: guardar volumen actual y poner a 0
            previousMusicVolume = musicVolume
            musicVolume = 0f
            MusicManager.setMusicVolume(context, 0f)
        } else {
            // Reactivar: restaurar volumen anterior
            musicVolume = previousMusicVolume
            MusicManager.setMusicVolume(context, previousMusicVolume)
            soundManager.playSelectSound()
        }
        Log.d("AjustesScreen", "Música ${if (musicVolume > 0f) "activada" else "silenciada"}")
    }

    val onSoundMuteToggle: () -> Unit = {
        if (soundVolume > 0f) {
            // Silenciar: guardar volumen actual y poner a 0
            previousSoundVolume = soundVolume
            soundVolume = 0f
            soundManager.setEffectsVolume(0f)
        } else {
            // Reactivar: restaurar volumen anterior
            soundVolume = previousSoundVolume
            soundManager.setEffectsVolume(previousSoundVolume)
        }
        Log.d("AjustesScreen", "Efectos ${if (soundVolume > 0f) "activados" else "silenciados"}")
    }

    // 🔹 NUEVO: Cargar volúmenes iniciales
    LaunchedEffect(Unit) {
        // Solo cargar volumen de efectos desde SoundManager
        soundVolume = soundManager.loadEffectsVolume()
        previousSoundVolume = soundVolume

        Log.d("AjustesScreen", "Ajustes inicializados - Efectos: $soundVolume")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1B4A),
                        Color(0xFF172B6F),
                        Color(0xFF233A89)
                    )
                )
            )
            .padding(24.dp)
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
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Ajustes",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            // Contenido desplazable
            androidx.compose.foundation.rememberScrollState().let { scrollState ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 🔹 SECCIÓN: Información del Usuario
                    UserInfoSection(
                        usuario = usuario,
                        onEditNameClick = { showEditNameDialog = true },
                        soundManager = soundManager
                    )

                    // 🔹 SECCIÓN: Audio - CORREGIDA (ahora con tipos explícitos)
                    AudioSettingsSection(
                        musicVolume = musicVolume,
                        soundVolume = soundVolume,
                        onMusicVolumeChange = onMusicVolumeChange,
                        onSoundVolumeChange = onSoundVolumeChange,
                        onMusicMuteToggle = onMusicMuteToggle,
                        onSoundMuteToggle = onSoundMuteToggle
                    )

                    // 🔹 SECCIÓN: Notificaciones
                    NotificationsSection(
                        notificationsEnabled = notificationsEnabled,
                        onNotificationsChange = { enabled ->
                            notificationsEnabled = enabled
                            toggleNotifications(context, enabled)
                            soundManager.playSelectSound()
                        }
                    )

                    // 🔹 SECCIÓN: Gameplay
                    GameplaySection(
                        vibrationEnabled = vibrationEnabled,
                        onVibrationChange = { enabled ->
                            vibrationEnabled = enabled
                            soundManager.playSelectSound()
                        }
                    )

                    // 🔹 SECCIÓN: Datos y Privacidad
                    DataPrivacySection(
                        onResetProgress = {
                            // Implementar reset de progreso
                            soundManager.playSelectSound()
                        }
                    )
                }
            }
        }

        // 🔹 DIÁLOGO: Editar nombre
        if (showEditNameDialog) {
            EditNameDialog(
                currentName = usuario?.nombre ?: "",
                onConfirm = { newName ->
                    gameViewModel.actualizarNombreUsuario(newName)
                    showEditNameDialog = false
                    soundManager.playSelectSound()
                },
                onDismiss = {
                    showEditNameDialog = false
                    soundManager.playSelectSound()
                }
            )
        }
    }
}

// 🔹 COMPONENTE: Información del Usuario
@Composable
private fun UserInfoSection(
    usuario: Usuario?,
    onEditNameClick: () -> Unit,
    soundManager: SoundManager
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x40FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Información del Jugador",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            // Nombre del usuario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Nombre",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = usuario?.nombre ?: "Jugador",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(
                    onClick = {
                        soundManager.playSelectSound()
                        onEditNameClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar nombre",
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Monedas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monedas",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${usuario?.monedas ?: 0}",
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.ic_coin),
                    contentDescription = "Monedas",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// 🔹 COMPONENTE: Configuración de Audio
@Composable
private fun AudioSettingsSection(
    musicVolume: Float,
    soundVolume: Float,
    onMusicVolumeChange: (Float) -> Unit,
    onSoundVolumeChange: (Float) -> Unit,
    onMusicMuteToggle: () -> Unit,
    onSoundMuteToggle: () -> Unit
) {
    // 🔹 DETERMINAR ICONOS BASADOS EN VOLUMEN (usando drawables)
    val musicIconRes = when {
        musicVolume == 0f -> R.drawable.volumeoff
        musicVolume < 0.3f -> R.drawable.volumedown
        musicVolume < 0.7f -> R.drawable.volumeup
        else -> R.drawable.volumeup
    }

    val soundIconRes = when {
        soundVolume == 0f -> R.drawable.volumeoff
        soundVolume < 0.3f -> R.drawable.volumedown
        soundVolume < 0.7f -> R.drawable.volumeup
        else -> R.drawable.volumeup
    }

    // Colores para los iconos
    val musicIconColor = if (musicVolume == 0f) Color.Red.copy(alpha = 0.8f) else Color.White
    val soundIconColor = if (soundVolume == 0f) Color.Red.copy(alpha = 0.8f) else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x40FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con icono de audio
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔹 CAMBIO: Usar painterResource para icono personalizado
                Image(
                    painter = painterResource(id = R.drawable.volumeup), // Tu icono de audio
                    contentDescription = "Audio",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Audio",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            // 🔹 VOLUMEN DE MÚSICA MEJORADO
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 🔹 CAMBIO: Icono de música personalizado
                        Image(
                            painter = painterResource(id = R.drawable.music), // Tu icono de música
                            contentDescription = "Música",
                            colorFilter = ColorFilter.tint(Color.White),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Música de fondo",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Porcentaje de volumen
                        Text(
                            text = if (musicVolume == 0f) "Mute" else "${(musicVolume * 100).toInt()}%",
                            color = if (musicVolume == 0f) Color.Red.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = if (musicVolume == 0f) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(Modifier.width(8.dp))
                        // Botón de mute
                        IconButton(
                            onClick = onMusicMuteToggle,
                            modifier = Modifier.size(32.dp)
                        ) {
                            // 🔹 CAMBIO: Usar Image en lugar de Icon para drawables
                            Image(
                                painter = painterResource(id = musicIconRes),
                                contentDescription = if (musicVolume == 0f) "Activar música" else "Silenciar música",
                                colorFilter = ColorFilter.tint(musicIconColor),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Slider(
                    value = musicVolume,
                    onValueChange = onMusicVolumeChange,
                    valueRange = 0f..1f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = if (musicVolume == 0f) Color.Red else Color(0xFF00E676),
                        activeTrackColor = if (musicVolume == 0f) Color.Red.copy(alpha = 0.5f) else Color(0xFF00E676),
                        inactiveTrackColor = if (musicVolume == 0f) Color.Red.copy(alpha = 0.2f) else Color(0xFF00E676).copy(alpha = 0.3f)
                    )
                )

                // Indicadores de volumen (opcional)
                if (musicVolume > 0f) {
                    Spacer(Modifier.height(4.dp))
                    VolumeBars(volume = musicVolume)
                }
            }

            Spacer(Modifier.height(20.dp))

            // 🔹 VOLUMEN DE EFECTOS MEJORADO
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 🔹 CAMBIO: Icono de efectos personalizado
                        Image(
                            painter = painterResource(id = R.drawable.volumedown), // Tu icono de efectos
                            contentDescription = "Efectos",
                            colorFilter = ColorFilter.tint(Color.White),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Efectos de sonido",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Porcentaje de volumen
                        Text(
                            text = if (soundVolume == 0f) "Mute" else "${(soundVolume * 100).toInt()}%",
                            color = if (soundVolume == 0f) Color.Red.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = if (soundVolume == 0f) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(Modifier.width(8.dp))
                        // Botón de mute
                        IconButton(
                            onClick = onSoundMuteToggle,
                            modifier = Modifier.size(32.dp)
                        ) {
                            // 🔹 CAMBIO: Usar Image en lugar de Icon para drawables
                            Image(
                                painter = painterResource(id = soundIconRes),
                                contentDescription = if (soundVolume == 0f) "Activar efectos" else "Silenciar efectos",
                                colorFilter = ColorFilter.tint(soundIconColor),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Slider(
                    value = soundVolume,
                    onValueChange = onSoundVolumeChange,
                    valueRange = 0f..1f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = if (soundVolume == 0f) Color.Red else Color(0xFF00E676),
                        activeTrackColor = if (soundVolume == 0f) Color.Red.copy(alpha = 0.5f) else Color(0xFF00E676),
                        inactiveTrackColor = if (soundVolume == 0f) Color.Red.copy(alpha = 0.2f) else Color(0xFF00E676).copy(alpha = 0.3f)
                    )
                )

                // Indicadores de volumen (opcional)
                if (soundVolume > 0f) {
                    Spacer(Modifier.height(4.dp))
                    VolumeBars(volume = soundVolume)
                }
            }

            // 🔹 BOTONES RÁPIDOS DE VOLUMEN
            Spacer(Modifier.height(16.dp))
            QuickVolumeControls(
                onMusicLow = { onMusicVolumeChange(0.3f) },
                onMusicMedium = { onMusicVolumeChange(0.6f) },
                onMusicHigh = { onMusicVolumeChange(1f) },
                onSoundLow = { onSoundVolumeChange(0.3f) },
                onSoundMedium = { onSoundVolumeChange(0.6f) },
                onSoundHigh = { onSoundVolumeChange(1f) }
            )
        }
    }
}

// 🔹 COMPONENTE: Barras de volumen visuales
@Composable
private fun VolumeBars(volume: Float) {
    val bars = 10
    val activeBars = (volume * bars).toInt()

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(bars) { index ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(8.dp)
                    .background(
                        color = if (index < activeBars) Color(0xFF00E676) else Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

// 🔹 COMPONENTE: Controles rápidos de volumen
@Composable
private fun QuickVolumeControls(
    onMusicLow: () -> Unit,
    onMusicMedium: () -> Unit,
    onMusicHigh: () -> Unit,
    onSoundLow: () -> Unit,
    onSoundMedium: () -> Unit,
    onSoundHigh: () -> Unit
) {
    Column {
        Text(
            text = "Ajustes rápidos:",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Controles para música
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Música",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(4.dp))
                Row {
                    QuickVolumeButton(level = "Bajo", onClick = onMusicLow)
                    Spacer(Modifier.width(4.dp))
                    QuickVolumeButton(level = "Medio", onClick = onMusicMedium)
                    Spacer(Modifier.width(4.dp))
                    QuickVolumeButton(level = "Alto", onClick = onMusicHigh)
                }
            }

            // Controles para efectos
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Efectos",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(4.dp))
                Row {
                    QuickVolumeButton(level = "Bajo", onClick = onSoundLow)
                    Spacer(Modifier.width(4.dp))
                    QuickVolumeButton(level = "Medio", onClick = onSoundMedium)
                    Spacer(Modifier.width(4.dp))
                    QuickVolumeButton(level = "Alto", onClick = onSoundHigh)
                }
            }
        }
    }
}

@Composable
private fun QuickVolumeButton(level: String, onClick: () -> Unit) {
    Text(
        text = level,
        color = Color.White,
        fontSize = 10.sp,
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                color = Color(0xFF00E676).copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

// 🔹 COMPONENTE: Notificaciones
@Composable
private fun NotificationsSection(
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x40FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Notificaciones",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            // Notificaciones de recordatorio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recordatorios para jugar",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Te recordamos volver al juego",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E676),
                        checkedTrackColor = Color(0xFF00E676).copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

// 🔹 COMPONENTE: Gameplay
@Composable
private fun GameplaySection(
    vibrationEnabled: Boolean,
    onVibrationChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x40FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Gameplay",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            // Vibración
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vibración",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Vibración al recolectar ítems",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = onVibrationChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E676),
                        checkedTrackColor = Color(0xFF00E676).copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}


// 🔹 COMPONENTE: Datos y Privacidad
@Composable
private fun DataPrivacySection(
    onResetProgress: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x40FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Datos y Privacidad",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            // Reset de progreso
            Button(
                onClick = onResetProgress,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reiniciar Progreso")
            }
        }
    }
}

// 🔹 DIÁLOGO: Editar nombre
@Composable
private fun EditNameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Nombre") },
        text = {
            Column {
                Text("Ingresa tu nuevo nombre:")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text("Tu nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newName) },
                enabled = newName.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// 🔹 FUNCIONES AUXILIARES
private fun updateMusicVolume(context: Context, volume: Float) {
    val intent = Intent(context, MusicService::class.java)
    intent.putExtra("action", "set_volume")
    intent.putExtra("volume", volume)

    // 🔹 IMPORTANTE: Usar startService en lugar de startService para Android 8+
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }

    Log.d("AjustesScreen", "Volumen de música actualizado a: ${(volume * 100).toInt()}%")
}

private fun toggleNotifications(context: Context, enabled: Boolean) {
    if (enabled) {
        // Programar Worker de notificaciones
        scheduleNotificationWorker(context)
    } else {
        // Cancelar Worker de notificaciones
        cancelNotificationWorker(context)
    }
}

private fun scheduleNotificationWorker(context: Context) {
    // Implementar Worker para notificaciones
    val constraints = androidx.work.Constraints.Builder()
        .setRequiresCharging(false)
        .build()

    val notificationRequest = androidx.work.PeriodicWorkRequestBuilder<NotificationWorker>(
        repeatInterval = 24, // 24 horas
        java.util.concurrent.TimeUnit.HOURS
    )
        .setConstraints(constraints)
        .build()

    androidx.work.WorkManager.getInstance(context).enqueue(notificationRequest)
}

private fun cancelNotificationWorker(context: Context) {
    androidx.work.WorkManager.getInstance(context).cancelAllWorkByTag("notification_worker")
}

// 🔹 WORKER para Notificaciones (crear archivo separado)
class NotificationWorker(
    context: Context,
    params: androidx.work.WorkerParameters
) : androidx.work.Worker(context, params) {

    override fun doWork(): Result {
        // Verificar cuándo fue la última vez que jugó
        val lastPlayed = getLastPlayedTime()
        val currentTime = System.currentTimeMillis()
        val daysSinceLastPlay = (currentTime - lastPlayed) / (24 * 60 * 60 * 1000)

        if (daysSinceLastPlay >= 2) { // Si no juega hace 2 días
            sendReminderNotification()
        }

        return Result.success()
    }

    private fun getLastPlayedTime(): Long {
        // Obtener de SharedPreferences
        val prefs = applicationContext.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("last_played", 0L)
    }

    private fun sendReminderNotification() {
        // Crear notificación de recordatorio
        // Implementar según tu canal de notificaciones
    }
}