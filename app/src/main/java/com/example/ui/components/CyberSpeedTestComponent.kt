package com.example.ui.components

import android.app.Activity
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobManager
import com.example.ads.AdMobUnits
import com.example.telemetry.NetworkSpeedTestManager
import com.example.telemetry.SpeedTestPhase
import com.example.ui.theme.BorderDim
import com.example.ui.theme.LocalAccentColor
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive Network Speed Test Card with animated circular speedometer gauge,
 * live upload and download measurements, ping, jitter, and quality rating.
 */
@Composable
fun CyberSpeedTestCard(
    speedTestManager: NetworkSpeedTestManager,
    adMobManager: AdMobManager,
    modifier: Modifier = Modifier
) {
    val state by speedTestManager.state.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val accent = LocalAccentColor.current

    val gaugeColor = when (state.phase) {
        SpeedTestPhase.DOWNLOAD -> Color(0xFF00E5FF)
        SpeedTestPhase.UPLOAD -> Color(0xFFD500F9)
        SpeedTestPhase.PING -> Color(0xFFFFAB00)
        SpeedTestPhase.COMPLETED -> Color(0xFF00E676)
        else -> accent
    }

    CyberCard(
        modifier = modifier,
        glowColor = gaugeColor
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(gaugeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = gaugeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TEST DE VELOCIDAD DE RED",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = gaugeColor
                        )
                        Text(
                            text = "Medición real de Descarga, Subida y Latencia",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                CyberStatusPill(
                    text = when (state.phase) {
                        SpeedTestPhase.IDLE -> "STANDBY"
                        SpeedTestPhase.PING -> "LATENCIA"
                        SpeedTestPhase.DOWNLOAD -> "DESCARGA"
                        SpeedTestPhase.UPLOAD -> "SUBIDA"
                        SpeedTestPhase.COMPLETED -> "FINALIZADO"
                        SpeedTestPhase.ERROR -> "ERROR"
                    },
                    isActive = state.isRunning || state.phase == SpeedTestPhase.COMPLETED,
                    activeColor = gaugeColor
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Central Speedometer Gauge
            CyberSpeedometerGauge(
                currentSpeed = state.currentSpeedMbps,
                maxSpeed = 120f,
                accentColor = gaugeColor,
                phase = state.phase,
                size = 190.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Indicator
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = state.statusMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = gaugeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${(state.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = gaugeColor,
                    trackColor = Color(0x33FFFFFF)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metric Results Cards: Download and Upload
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Download Metric Card
                SpeedMetricBox(
                    title = "DESCARGA",
                    value = "${state.downloadSpeedMbps}",
                    unit = "Mbps",
                    icon = Icons.Default.ArrowDownward,
                    color = Color(0xFF00E5FF),
                    isActive = state.phase == SpeedTestPhase.DOWNLOAD,
                    modifier = Modifier.weight(1f)
                )

                // Upload Metric Card
                SpeedMetricBox(
                    title = "SUBIDA",
                    value = "${state.uploadSpeedMbps}",
                    unit = "Mbps",
                    icon = Icons.Default.ArrowUpward,
                    color = Color(0xFFD500F9),
                    isActive = state.phase == SpeedTestPhase.UPLOAD,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Metrics: Ping, Jitter & Quality
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0x33080E1C),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDim)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFFFAB00), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("PING", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                            Text("${state.pingMs} ms", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0x33080E1C),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDim)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("JITTER", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                            Text("${state.jitterMs} ms", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Quality Rating Badge if Completed
            AnimatedVisibility(visible = state.phase == SpeedTestPhase.COMPLETED) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    color = Color(0x2200E676),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6600E676))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "CALIFICACIÓN DE CONEXIÓN",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Text(
                                text = state.qualityRating,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button with AdMob Interstitial Trigger
            Button(
                onClick = {
                    if (!state.isRunning) {
                        val activity = context as? Activity
                        val startTest: () -> Unit = {
                            try {
                                val vibrator = context.getSystemService(Vibrator::class.java)
                                vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                            } catch (ignored: Exception) {}
                            scope.launch {
                                speedTestManager.runSpeedTest()
                            }
                        }

                        if (activity != null) {
                            adMobManager.showInterstitialForFeature(
                                activity = activity,
                                adUnitId = AdMobUnits.INTERSTITIAL_OPTIMIZER,
                                featureName = "Test de Velocidad de Red (Download & Upload)",
                                onDone = startTest
                            )
                        } else {
                            startTest()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = gaugeColor,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isRunning
            ) {
                if (state.isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (state.phase) {
                            SpeedTestPhase.PING -> "Calculando Latencia..."
                            SpeedTestPhase.DOWNLOAD -> "Midiendo Descarga..."
                            SpeedTestPhase.UPLOAD -> "Midiendo Subida..."
                            else -> "Midiendo..."
                        },
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.phase == SpeedTestPhase.COMPLETED) "REPETIR TEST DE VELOCIDAD" else "INICIAR TEST DE VELOCIDAD",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Animated Circular Speedometer Gauge with sweeping radial ticks and digital readout
 */
@Composable
fun CyberSpeedometerGauge(
    currentSpeed: Float,
    maxSpeed: Float = 120f,
    accentColor: Color,
    phase: SpeedTestPhase,
    size: Dp = 190.dp
) {
    val speedRatio = (currentSpeed / maxSpeed).coerceIn(0f, 1f)
    val animatedRatio by animateFloatAsState(
        targetValue = speedRatio,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "speed_ratio"
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = 14.dp.toPx()
            val startAngle = 135f
            val sweepTotal = 270f
            val radius = (this.size.minDimension - strokePx) / 2f
            val centerOffset = Offset(this.size.width / 2f, this.size.height / 2f)

            // Background Track Arc
            drawArc(
                color = Color(0x22FFFFFF),
                startAngle = startAngle,
                sweepAngle = sweepTotal,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active Sweep Arc
            val activeSweep = sweepTotal * animatedRatio
            if (activeSweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.3f),
                            accentColor,
                            accentColor
                        )
                    ),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            // Radial Speed Tick Marks
            val tickCount = 12
            for (i in 0..tickCount) {
                val tickAngleDeg = startAngle + (sweepTotal * (i.toFloat() / tickCount))
                val tickAngleRad = Math.toRadians(tickAngleDeg.toDouble())
                val innerR = radius - strokePx - 4.dp.toPx()
                val outerR = radius - strokePx + 4.dp.toPx()

                val startTick = Offset(
                    x = (centerOffset.x + innerR * cos(tickAngleRad)).toFloat(),
                    y = (centerOffset.y + innerR * sin(tickAngleRad)).toFloat()
                )
                val endTick = Offset(
                    x = (centerOffset.x + outerR * cos(tickAngleRad)).toFloat(),
                    y = (centerOffset.y + outerR * sin(tickAngleRad)).toFloat()
                )

                val isPassed = (i.toFloat() / tickCount) <= animatedRatio
                drawLine(
                    color = if (isPassed) accentColor else Color(0x44FFFFFF),
                    start = startTick,
                    end = endTick,
                    strokeWidth = if (i % 3 == 0) 2.5f else 1.2f
                )
            }
        }

        // Digital Speed Readout in Center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (phase) {
                    SpeedTestPhase.DOWNLOAD -> Icons.Default.ArrowDownward
                    SpeedTestPhase.UPLOAD -> Icons.Default.ArrowUpward
                    SpeedTestPhase.PING -> Icons.Default.Timer
                    else -> Icons.Default.Speed
                },
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = String.format("%.1f", currentSpeed).replace(',', '.'),
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )

            Text(
                text = "Mbps",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )

            Text(
                text = when (phase) {
                    SpeedTestPhase.DOWNLOAD -> "DESCARGA ACTIVA"
                    SpeedTestPhase.UPLOAD -> "SUBIDA ACTIVA"
                    SpeedTestPhase.PING -> "CALCULANDO PING"
                    SpeedTestPhase.COMPLETED -> "RESULTADO FINAL"
                    else -> "VELOCIDAD"
                },
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SpeedMetricBox(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = if (isActive) color.copy(alpha = 0.15f) else Color(0x33080E1C),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) color else BorderDim
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}
