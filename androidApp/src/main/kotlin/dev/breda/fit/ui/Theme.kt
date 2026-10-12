package dev.breda.fit.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Lime = Color(0xFFA7F36B)
val Ink = Color(0xFF101817)
val Panel = Color(0xFF1B2724)
val Mist = Color(0xFFC1CEC7)
val Blue = Color(0xFF89B7FF)
val Coral = Color(0xFFFFAD91)
private val Palette = darkColorScheme(primary = Lime, onPrimary = Ink, secondary = Blue, background = Ink, surface = Panel, onSurface = Color(0xFFF3F6EF), onBackground = Color(0xFFF3F6EF), surfaceVariant = Color(0xFF293830), onSurfaceVariant = Mist, error = Coral)
@Composable fun FitTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Palette, typography = Typography(
        displaySmall = TextStyle(fontSize = 38.sp, fontWeight = FontWeight.Bold),
        headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold),
        titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
        bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp)
    ), content = content)
}
