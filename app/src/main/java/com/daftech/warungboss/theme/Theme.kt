package com.daftech.warungboss.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Cream = Color(0xFFFFF8F0)
val CreamDark = Color(0xFF1A1210)
val Bata = Color(0xFFC1272D)
val BataDark = Color(0xFFE0484D)
val Emas = Color(0xFFC9A227)
val EmasSoft = Color(0xFFFFF3C4)
val Arang = Color(0xFF1A1A1A)
val ArangSoft = Color(0xFF6B5D55)
val Hijau = Color(0xFF1E9E5A)
val Kuning = Color(0xFFE8A100)
val CardLight = Color(0xFFFFFFFF)
val CardDark = Color(0xFF2A201D)

private val LightScheme = lightColorScheme(
    primary = Bata,
    onPrimary = Color.White,
    secondary = Emas,
    onSecondary = Arang,
    background = Cream,
    onBackground = Arang,
    surface = CardLight,
    onSurface = Arang,
    tertiary = Hijau
)
private val DarkScheme = darkColorScheme(
    primary = BataDark,
    onPrimary = Color.White,
    secondary = Emas,
    onSecondary = Arang,
    background = CreamDark,
    onBackground = Color.White,
    surface = CardDark,
    onSurface = Color.White,
    tertiary = Hijau
)

@Composable
fun WarungBossTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = androidx.compose.material3.Typography(
            displayLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp),
            titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 20.sp),
            titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 16.sp),
            bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontSize = 16.sp),
            bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontSize = 14.sp),
            labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        ),
        content = content
    )
}
