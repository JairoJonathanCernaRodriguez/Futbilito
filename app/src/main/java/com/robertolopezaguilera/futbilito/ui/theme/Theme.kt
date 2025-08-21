package com.robertolopezaguilera.futbilito.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E3440),
    onPrimary = Color.White,
    secondary = Color(0xFF5E81AC),
    onSecondary = Color.White,
    tertiary = Color(0xFFBF616A),
    background = Color(0xFFECEFF4),
    surface = Color(0xFFD8DEE9),
    onBackground = Color(0xFF2E3440),
    onSurface = Color(0xFF2E3440),
    error = Color(0xFFD08770)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B4252),
    onPrimary = Color.White,
    secondary = Color(0xFF81A1C1),
    onSecondary = Color.White,
    tertiary = Color(0xFFD08770),
    background = Color(0xFF2E3440),
    surface = Color(0xFF3B4252),
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFBF616A)
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)

// Definimos las formas directamente sin depender de MaterialTheme
private val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

object MazeColors {
    val ballColor = Color(0xFFBF616A)
    val coinColor = Color(0xFFA3BE8C)
    val wallColor = Color(0xFF5E81AC)
    val timerCritical = Color(0xFFD08770)
    val timerWarning = Color(0xFFEBCB8B)
}

@Composable
fun FutbilitoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}