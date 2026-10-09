package com.example.ui.screens.boot

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ScientificBackground
import com.example.ui.theme.BorderDim
import com.example.ui.theme.LocalAccentColor
import kotlinx.coroutines.delay

@Composable
fun CyberBootScreen(
    onBootComplete: () -> Unit
) {
    val context = LocalContext.current
    val accent = LocalAccentColor.current
    val progress = remember { Animatable(0f) }
    val bootLogs = remember { mutableStateListOf<String>() }

    var currentScanPhase by remember { mutableStateOf("INICIALIZANDO NÚCLEO") }
    var hexStream by remember { mutableStateOf("0x4F8A 0x9D02 0x7E11 0x00A5") }
    var isTransitioningOut by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "core_pulse")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "core_rotation"
    )

    val laserScanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_scan"
    )

    LaunchedEffect(Unit) {
        val steps = listOf(
            Triple(0.15f, "FASE 1: ESCANEO BIOMÉTRICO Y HARDWARE", "» BIOS: ANDROSUITE QUANTUM-CORE v2.8 CARGADO"),
            Triple(0.35f, "FASE 2: CALIBRACIÓN DE SENSORES Y ANTENA", "» HARDWARE: ACELERÓMETRO, EMF Y GIROSCOPIO [CALIBRADOS]"),
            Triple(0.55f, "FASE 3: BLINDAJE ANTI-ESPÍA Y AUDITORÍA", "» SEGURIDAD: DETECTOR DE CÁMARAS OCULTAS Y BÓVEDA ROOM [ARMADOS]"),
            Triple(0.75f, "FASE 4: ESTABILIZACIÓN DE RED Y DATOS", "» TELECOM: PURGA DNS Y PRIORIDAD QOS FOREGROUND [ÓPTIMO]"),
            Triple(0.92f, "FASE 5: MALLA CRIPTOGRÁFICA EN NUBE", "» NUBE: FIRESTORE REALTIME SYNC Y RESPALDO ENCRIPTADO [LISTO]"),
            Triple(1.00f, "ACCESO AUTORIZADO", "» PROTOCOLO COMPLETO: BIENVENIDO AL SISTEMA")
        )

        val hexList = listOf(
            "0x8F9A 0x3E11 0x004B 0x7A9D",
            "0xC4E2 0x1A09 0xF9D1 0x882A",
            "0x55A1 0xBE99 0x24C8 0x6E03",
            "0x7B12 0xAA04 0xEE41 0x110F",
            "0x99FF 0x00D7 0x88BC 0x42AA"
        )

        for ((index, step) in steps.withIndex()) {
            val (targetProg, phase, log) = step
            currentScanPhase = phase
            hexStream = hexList[index % hexList.size]
            bootLogs.add(log)
            progress.animateTo(
                targetValue = targetProg,
                animationSpec = tween(durationMillis = 320, easing = LinearEasing)
            )
            delay(240)
        }

        try {
            val vibrator = context.getSystemService(Vibrator::class.java)
            vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (ignored: Exception) {}

        delay(350)
        isTransitioningOut = true
        delay(300)
        onBootComplete()
    }

    AnimatedVisibility(
        visible = !isTransitioningOut,
        exit = fadeOut(tween(300))
    ) {
        ScientificBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Futuristic rotating holographic reactor HUD with scanning laser
                    Box(
                        modifier = Modifier.size(170.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(170.dp)) {
                            val center = Offset(size.width / 2, size.height / 2)

                            // Outer spinning dashed ring
                            drawArc(
                                brush = Brush.sweepGradient(listOf(accent.copy(alpha = 0.1f), accent, accent.copy(alpha = 0.1f))),
                                startAngle = rotation,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Reverse spinning inner arc
                            drawArc(
                                brush = Brush.sweepGradient(listOf(Color(0xFF00E676), Color.Transparent, Color(0xFF00E676))),
                                startAngle = -rotation * 1.5f,
                                sweepAngle = 180f,
                                useCenter = false,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Inner radar grid ring
                            drawCircle(
                                color = accent.copy(alpha = 0.15f),
                                radius = size.width / 2.7f,
                                style = Stroke(width = 1.5.dp.toPx())
                            )

                            // Animated vertical laser sweep
                            val laserY = size.height * laserScanY
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    listOf(Color.Transparent, accent, Color.White, accent, Color.Transparent)
                                ),
                                start = Offset(size.width * 0.15f, laserY),
                                end = Offset(size.width * 0.85f, laserY),
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.15f))
                                .border(2.dp, accent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Núcleo de Seguridad",
                                tint = accent,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "ANDROSUITE PRO",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "SISTEMA CIBERNÉTICO & BLINDAJE TOTAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Phase label
                    Surface(
                        color = Color(0x66080E1C),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = currentScanPhase,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // High-tech Progress Bar
                    LinearProgressIndicator(
                        progress = { progress.value },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = accent,
                        trackColor = Color(0x33FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = hexStream,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(progress.value * 100).toInt()}% INICIALIZADO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB0BEC5),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Futuristic Terminal Log Window
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(125.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xAA080E1C),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDim)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            bootLogs.takeLast(4).forEach { logLine ->
                                Text(
                                    text = logLine,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (logLine.contains("AUTORIZADO") || logLine.contains("COMPLETO")) Color(0xFF00E676) else Color(0xFF90CAF9),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onBootComplete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = accent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Acceder al Menú Principal")
                    }
                }
            }
        }
    }
}
