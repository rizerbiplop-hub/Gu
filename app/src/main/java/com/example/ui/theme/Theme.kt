package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RivonColorScheme = darkColorScheme(
    primary = RivonElectricBlue,
    onPrimary = RivonDeepBlack,
    primaryContainer = RivonSurfaceElevated,
    onPrimaryContainer = RivonCyan,
    secondary = RivonCyan,
    onSecondary = RivonDeepBlack,
    secondaryContainer = RivonSurfaceElevated,
    onSecondaryContainer = RivonSoftCyan,
    tertiary = RivonNeonPurple,
    onTertiary = RivonDeepBlack,
    background = RivonDeepBlack,
    onBackground = RivonTextPrimary,
    surface = RivonMidnight,
    onSurface = RivonTextPrimary,
    surfaceVariant = RivonSurface,
    onSurfaceVariant = RivonTextSecondary,
    outline = RivonBorder,
    error = RivonErrorRed,
    onError = RivonDeepBlack
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RivonColorScheme,
        typography = Typography,
        content = content
    )
}
