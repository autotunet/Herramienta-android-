package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LocalCustomTheme = staticCompositionLocalOf { CustomDarkTheme.OLED_BLACK }
val LocalAccentColor = staticCompositionLocalOf { NeonCyan }

fun getCyberColorScheme(
    theme: CustomDarkTheme,
    accent: Color,
    systemDark: Boolean
): ColorScheme {
    return when (theme) {
        CustomDarkTheme.OLED_BLACK -> darkColorScheme(
            primary = accent,
            onPrimary = Color.Black,
            primaryContainer = accent.copy(alpha = 0.2f),
            onPrimaryContainer = accent,
            secondary = ElectricBlue,
            onSecondary = Color.Black,
            secondaryContainer = ElectricBlue.copy(alpha = 0.15f),
            onSecondaryContainer = ElectricBlue,
            tertiary = CyberViolet,
            background = OledBlack,
            onBackground = Color(0xFFF0F4F8),
            surface = OledSurface,
            onSurface = Color(0xFFF0F4F8),
            surfaceVariant = OledSurfaceVariant,
            onSurfaceVariant = Color(0xFFB0B7C3),
            outline = accent.copy(alpha = 0.4f),
            outlineVariant = Color(0xFF232630)
        )
        CustomDarkTheme.CYBERPUNK_NEON -> darkColorScheme(
            primary = accent,
            onPrimary = Color.Black,
            primaryContainer = accent.copy(alpha = 0.25f),
            onPrimaryContainer = accent,
            secondary = CyberViolet,
            onSecondary = Color.White,
            secondaryContainer = CyberViolet.copy(alpha = 0.2f),
            onSecondaryContainer = CyberViolet,
            tertiary = NeonAmber,
            background = CyberpunkBg,
            onBackground = Color(0xFFF3F6FC),
            surface = CyberpunkSurface,
            onSurface = Color(0xFFF3F6FC),
            surfaceVariant = CyberpunkSurfaceVariant,
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = accent.copy(alpha = 0.5f),
            outlineVariant = Color(0xFF334155)
        )
        CustomDarkTheme.MATRIX_HACKER -> darkColorScheme(
            primary = MatrixGreen,
            onPrimary = Color.Black,
            primaryContainer = MatrixGreen.copy(alpha = 0.2f),
            onPrimaryContainer = MatrixGreen,
            secondary = MatrixGreen,
            onSecondary = Color.Black,
            secondaryContainer = MatrixGreen.copy(alpha = 0.15f),
            onSecondaryContainer = MatrixGreen,
            tertiary = NeonCyan,
            background = MatrixBg,
            onBackground = Color(0xFFE0FFE5),
            surface = MatrixSurface,
            onSurface = Color(0xFFE0FFE5),
            surfaceVariant = MatrixSurfaceVariant,
            onSurfaceVariant = Color(0xFF80C090),
            outline = MatrixGreen.copy(alpha = 0.4f),
            outlineVariant = Color(0xFF1B4025)
        )
        CustomDarkTheme.DEEP_SLATE -> darkColorScheme(
            primary = accent,
            onPrimary = Color.Black,
            primaryContainer = accent.copy(alpha = 0.2f),
            onPrimaryContainer = accent,
            secondary = ElectricBlue,
            onSecondary = Color.Black,
            secondaryContainer = ElectricBlue.copy(alpha = 0.15f),
            onSecondaryContainer = ElectricBlue,
            tertiary = CyberViolet,
            background = SlateBg,
            onBackground = Color(0xFFE6EDF8),
            surface = SlateSurface,
            onSurface = Color(0xFFE6EDF8),
            surfaceVariant = SlateSurfaceVariant,
            onSurfaceVariant = Color(0xFF8FA3BF),
            outline = accent.copy(alpha = 0.35f),
            outlineVariant = Color(0xFF26354D)
        )
        CustomDarkTheme.SYSTEM -> {
            if (systemDark) {
                darkColorScheme(
                    primary = accent,
                    background = Color(0xFF111418),
                    surface = Color(0xFF191D24)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF00677D),
                    background = Color(0xFFF8F9FA),
                    surface = Color(0xFFFFFFFF)
                )
            }
        }
    }
}

@Composable
fun AndroSuiteTheme(
    selectedTheme: CustomDarkTheme = CustomDarkTheme.CYBERPUNK_NEON,
    selectedAccent: AccentTheme = AccentTheme.CYAN,
    systemDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = getCyberColorScheme(
        theme = selectedTheme,
        accent = selectedAccent.color,
        systemDark = systemDark
    )

    CompositionLocalProvider(
        LocalCustomTheme provides selectedTheme,
        LocalAccentColor provides selectedAccent.color
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AndroTypography,
            content = content
        )
    }
}
