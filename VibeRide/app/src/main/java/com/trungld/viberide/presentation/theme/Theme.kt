package com.trungld.viberide.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Composable
fun VibeRideTheme(content: @Composable () -> Unit) {
    val darkColors = darkColorScheme(
        primary = Color(0xFFBB86FC),        // A vibrant purple
        secondary = Color(0xFF03DAC6),      // A teal accent
        background = Color(0xFF121212),     // Dark gray background (YouTube Music style)
        surface = Color(0xFF121212),        // Matches background for consistency
        onPrimary = Color.Black,            // Text/icon color on primary
        onSecondary = Color.Black,          // Text/icon color on secondary
        onBackground = Color.White,         // Text/icon color on background
        onSurface = Color.White             // Text/icon color on surface
    )
    MaterialTheme(
        colorScheme = darkColors,
        typography = Typography,            // Default or custom typography
        content = content
    )
}