package com.mdavis8403.magickingdomtrivia.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val TriviaColorScheme = darkColorScheme(
    primary = Color(0xFFF5C86A),
    onPrimary = Color(0xFF211704),
    secondary = Color(0xFF79E6BE),
    onSecondary = Color(0xFF04241A),
    tertiary = Color(0xFFFF8E9E),
    background = Color(0xFF07111F),
    onBackground = Color(0xFFF4F7FC),
    surface = Color(0xFF192941),
    onSurface = Color(0xFFF4F7FC),
    surfaceVariant = Color(0xFF243650),
    onSurfaceVariant = Color(0xFFCAD5E4),
    error = Color(0xFFFF8E9E),
)

@Composable
fun MagicKingdomTriviaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TriviaColorScheme,
        content = content,
    )
}
