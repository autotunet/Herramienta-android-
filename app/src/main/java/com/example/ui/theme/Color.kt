package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Futuristic Cyberpunk & Neon Core Palette
val NeonCyan = Color(0xFF00E5FF)
val ElectricBlue = Color(0xFF00B0FF)
val MatrixGreen = Color(0xFF00E676)
val CyberViolet = Color(0xFFD500F9)
val NeonPurple = Color(0xFF7C4DFF)
val NeonAmber = Color(0xFFFFAB00)
val PlasmaPink = Color(0xFFFF1744)

// Dark Theme Backgrounds & Surfaces
val OledBlack = Color(0xFF000000)
val OledSurface = Color(0xFF0D0E12)
val OledSurfaceVariant = Color(0xFF161820)

val CyberpunkBg = Color(0xFF070B14)
val CyberpunkSurface = Color(0xFF0F172A)
val CyberpunkSurfaceVariant = Color(0xFF1E293B)

val MatrixBg = Color(0xFF040D07)
val MatrixSurface = Color(0xFF0A1F11)
val MatrixSurfaceVariant = Color(0xFF122E1B)

val SlateBg = Color(0xFF0A0F1D)
val SlateSurface = Color(0xFF131B2E)
val SlateSurfaceVariant = Color(0xFF1E2942)

// Card & Border Glows
val NeonCyanGlow = Color(0x3300E5FF)
val MatrixGreenGlow = Color(0x3300E676)
val CyberVioletGlow = Color(0x33D500F9)
val BorderDim = Color(0x22FFFFFF)
val BorderBright = Color(0x4400E5FF)

enum class CustomDarkTheme(val displayName: String, val description: String) {
    OLED_BLACK("OLED Puro", "Negro absoluto para máximo ahorro de batería"),
    CYBERPUNK_NEON("Cyberpunk Neon", "Glow cian y magenta de alta tecnología"),
    MATRIX_HACKER("Matrix Hacker", "Estilo consola de terminal fosforescente"),
    DEEP_SLATE("Deep Slate", "Azul marino profundo espacial y minimalista"),
    SYSTEM("Sistema M3", "Sigue el modo oscuro del sistema Android")
}

enum class AccentTheme(val displayName: String, val color: Color) {
    CYAN("Cian Neón", NeonCyan),
    GREEN("Matrix Verde", MatrixGreen),
    VIOLET("Cyber Violeta", CyberViolet),
    AMBER("Ámbar Eléctrico", NeonAmber),
    PINK("Plasma Rosa", PlasmaPink)
}
