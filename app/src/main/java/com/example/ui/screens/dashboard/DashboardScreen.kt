package com.example.ui.screens.dashboard

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ads.AdMobManager
import com.example.ads.AdMobUnits
import com.example.security.CyberPermissionStatusCard
import com.example.security.CyberSecurityManager
import com.example.telemetry.SignalBoosterManager
import com.example.telemetry.SystemTelemetryManager
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberGauge
import com.example.ui.components.CyberMetricTile
import com.example.ui.components.CyberStatusPill
import com.example.ui.theme.BorderDim
import com.example.ui.theme.LocalAccentColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
    telemetryManager: SystemTelemetryManager,
    signalBooster: SignalBoosterManager,
    securityManager: CyberSecurityManager,
    adMobManager: AdMobManager,
    permissionsState: MultiplePermissionsState? = null,
    onOpenPermissions: () -> Unit = {},
    onNavigateToProductivity: () -> Unit,
    onNavigateToOptimizer: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToAI: () -> Unit,
    onNavigateToSync: () -> Unit,
    onNavigateToVault: () -> Unit = {}
) {
    val battery by telemetryManager.batteryState.collectAsState()
    val memory by telemetryManager.memoryState.collectAsState()
    val storage by telemetryManager.storageState.collectAsState()
    val network by telemetryManager.networkState.collectAsState()
    val signal by signalBooster.signalState.collectAsState()
    val isShieldActive by securityManager.isShieldActive.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCleaningRam by remember { mutableStateOf(false) }
    var cleanSuccessMessage by remember { mutableStateOf<String?>(null) }
    var isBoostingSignal by remember { mutableStateOf(false) }
    var signalBoostMsg by remember { mutableStateOf<String?>(null) }
    val accent = LocalAccentColor.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Futuristic Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(accent, BorderDim, accent.copy(alpha = 0.5f))),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cyber_hero),
                    contentDescription = "Holograma HUD",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xCC000000), Color(0xEE090D16))
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CyberStatusPill(text = "BLINDAJE ACTIVO", isActive = isShieldActive, activeColor = Color(0xFF00E676))
                            Spacer(modifier = Modifier.width(8.dp))
                            CyberStatusPill(
                                text = "${signal.carrierName.take(12)} • ${signal.signalStrengthPercent}%",
                                isActive = true,
                                activeColor = Color(0xFF00E5FF)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ANDROSUITE PRO HUD",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Seguridad blindada, amplificador de señal y herramientas de alto rendimiento",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB0BEC5)
                        )
                    }
                }
            }
        }

        // Accompanist Permissions Runtime Status Card for Total Device Control
        if (permissionsState != null) {
            item {
                CyberPermissionStatusCard(
                    permissionsState = permissionsState,
                    onOpenHub = onOpenPermissions
                )
            }
        }

        // Escudo de Seguridad & Anti-Espionaje Card
        item {
            CyberCard(glowColor = Color(0xFF00E676), onClick = onNavigateToSecurity) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0x2200E676)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ESCUDO ANTI-ESPIONAJE ACTIVO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                        Text(
                            text = "Bloqueo de cámaras/micrófonos ocultos y análisis de apps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            val activity = context as? android.app.Activity
                            if (activity != null) {
                                adMobManager.showInterstitialForFeature(
                                    activity = activity,
                                    adUnitId = AdMobUnits.INTERSTITIAL_SECURITY,
                                    featureName = "Escudo de Seguridad Anti-Espía",
                                    onDone = onNavigateToSecurity
                                )
                            } else onNavigateToSecurity()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = Color(0xFF00E676)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Verificar", fontSize = 11.sp)
                    }
                }
            }
        }

        // Bóveda Cifrada Local (Room) Card
        item {
            CyberCard(glowColor = Color(0xFFD500F9), onClick = {
                val activity = context as? android.app.Activity
                if (activity != null) {
                    adMobManager.showInterstitialForFeature(
                        activity = activity,
                        adUnitId = AdMobUnits.INTERSTITIAL_VAULT,
                        featureName = "Bóveda Cifrada Local",
                        onDone = onNavigateToVault
                    )
                } else onNavigateToVault()
            }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0x22D500F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFD500F9), modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BÓVEDA CIFRADA (ROOM)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD500F9)
                        )
                        Text(
                            text = "Contraseñas y notas secretas con desbloqueo biométrico",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            val activity = context as? android.app.Activity
                            if (activity != null) {
                                adMobManager.showInterstitialForFeature(
                                    activity = activity,
                                    adUnitId = AdMobUnits.INTERSTITIAL_VAULT,
                                    featureName = "Bóveda Cifrada Local",
                                    onDone = onNavigateToVault
                                )
                            } else onNavigateToVault()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = Color(0xFFD500F9)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Acceder", fontSize = 11.sp)
                    }
                }
            }
        }

        // Potenciador de Wi-Fi y Datos Móviles (Multi-Compañía)
        item {
            CyberCard(glowColor = Color(0xFF00E5FF)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF00E5FF))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "POTENCIA WI-FI & DATOS",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF00E5FF)
                                )
                                Text(
                                    text = "${signal.carrierName} • Señal: ${signal.signalStrengthPercent}% (${signal.signalDbm} dBm)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (!isBoostingSignal) {
                                    val activity = context as? android.app.Activity
                                    val action: () -> Unit = {
                                        isBoostingSignal = true
                                        scope.launch {
                                            val boosted = signalBooster.boostSignal()
                                            try {
                                                val vibrator = context.getSystemService(Vibrator::class.java)
                                                vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                                            } catch (ignored: Exception) {}
                                            signalBoostMsg = "¡Señal amplificada al ${boosted.signalStrengthPercent}%! Latencia reducida a ${boosted.currentPingMs} ms."
                                            isBoostingSignal = false
                                        }
                                    }
                                    if (activity != null) {
                                        adMobManager.showInterstitialForFeature(
                                            activity = activity,
                                            adUnitId = AdMobUnits.INTERSTITIAL_TAXIGORD_V1,
                                            featureName = "Potenciador de Señal Wi-Fi (Taxigord v1)",
                                            onDone = action
                                        )
                                    } else {
                                        action()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isBoostingSignal
                        ) {
                            if (isBoostingSignal) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                            } else {
                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aumentar")
                            }
                        }
                    }

                    AnimatedVisibility(visible = signalBoostMsg != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            color = Color(0x2200E676),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6600E676))
                        ) {
                            Text(
                                text = signalBoostMsg ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF00E676),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Boost & RAM Clean Action
        item {
            CyberCard(glowColor = accent) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ESTADO DE RENDIMIENTO",
                                style = MaterialTheme.typography.labelLarge,
                                color = accent
                            )
                            Text(
                                text = "RAM en uso: ${memory.usedPercent}% (${memory.usedRamMb} MB / ${memory.totalRamMb} MB)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = {
                                if (!isCleaningRam) {
                                    val activity = context as? android.app.Activity
                                    if (activity != null) {
                                        adMobManager.showInterstitialForFeature(
                                            activity = activity,
                                            adUnitId = AdMobUnits.INTERSTITIAL_OPTIMIZER,
                                            featureName = "Limpieza de Memoria RAM"
                                        ) {
                                            isCleaningRam = true
                                            scope.launch {
                                                val freed = telemetryManager.performRamClean()
                                                try {
                                                    val vibrator = context.getSystemService(Vibrator::class.java)
                                                    vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                                                } catch (ignored: Exception) {}
                                                cleanSuccessMessage = "¡Limpieza completada! Se liberaron ${freed} MB de memoria caché"
                                                isCleaningRam = false
                                            }
                                        }
                                    } else {
                                        isCleaningRam = true
                                        scope.launch {
                                            val freed = telemetryManager.performRamClean()
                                            cleanSuccessMessage = "¡Limpieza completada! Se liberaron ${freed} MB de memoria caché"
                                            isCleaningRam = false
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accent,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isCleaningRam
                        ) {
                            if (isCleaningRam) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.Black
                                )
                            } else {
                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Optimizar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    AnimatedVisibility(visible = cleanSuccessMessage != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            color = Color(0x2200E676),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6600E676))
                        ) {
                            Text(
                                text = cleanSuccessMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF00E676),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Live Telemetry HUD Gauges Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Battery Gauge
                CyberCard(
                    modifier = Modifier.weight(1f),
                    glowColor = Color(0xFF00E676),
                    onClick = onNavigateToOptimizer
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CyberGauge(
                            percentage = battery.levelPercent,
                            label = if (battery.isCharging) "Cargando" else "Batería",
                            size = 96.dp,
                            strokeWidth = 8.dp,
                            accentColor = Color(0xFF00E676)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${battery.temperatureCelsius}°C  •  ${battery.healthStatus}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Storage Gauge
                CyberCard(
                    modifier = Modifier.weight(1f),
                    glowColor = Color(0xFFD500F9),
                    onClick = onNavigateToOptimizer
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CyberGauge(
                            percentage = storage.usedPercent,
                            label = "Almacenamiento",
                            size = 96.dp,
                            strokeWidth = 8.dp,
                            accentColor = Color(0xFFD500F9)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${storage.freeGb} GB libres",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Navigation Tech Grid
        item {
            Text(
                text = "SUITE DE HERRAMIENTAS",
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            val activity = context as? android.app.Activity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Seguridad Blindada",
                    subtitle = "Detector de Cámaras & Espías",
                    icon = Icons.Default.Shield,
                    color = Color(0xFF00E676),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (activity != null) {
                            adMobManager.showInterstitialForFeature(
                                activity = activity,
                                adUnitId = AdMobUnits.INTERSTITIAL_SECURITY,
                                featureName = "Escudo Blindado Anti-Espía",
                                onDone = onNavigateToSecurity
                            )
                        } else onNavigateToSecurity()
                    }
                )
                QuickActionCard(
                    title = "Potencia de Señal",
                    subtitle = "Acelerar Wi-Fi y Datos",
                    icon = Icons.Default.Wifi,
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (activity != null) {
                            adMobManager.showInterstitialForFeature(
                                activity = activity,
                                adUnitId = AdMobUnits.INTERSTITIAL_TAXIGORD_V1,
                                featureName = "Potenciador de Señal Wi-Fi",
                                onDone = onNavigateToOptimizer
                            )
                        } else onNavigateToOptimizer()
                    }
                )
            }
        }

        item {
            val activity = context as? android.app.Activity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Inteligencia Artificial",
                    subtitle = "Gemini 2.5 Flash Asistente",
                    icon = Icons.Default.AutoAwesome,
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (activity != null) {
                            adMobManager.showInterstitialForFeature(
                                activity = activity,
                                adUnitId = AdMobUnits.INTERSTITIAL_AI,
                                featureName = "IA Gemini Asistente",
                                onDone = onNavigateToAI
                            )
                        } else onNavigateToAI()
                    }
                )
                QuickActionCard(
                    title = "Productividad",
                    subtitle = "Notas, Tareas y Pomodoro",
                    icon = Icons.Default.Timer,
                    color = Color(0xFFFFAB00),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (activity != null) {
                            adMobManager.showInterstitialForFeature(
                                activity = activity,
                                adUnitId = AdMobUnits.INTERSTITIAL_TAXIGORD_V1,
                                featureName = "Suite de Productividad",
                                onDone = onNavigateToProductivity
                            )
                        } else onNavigateToProductivity()
                    }
                )
            }
        }

        item {
            val activity = context as? android.app.Activity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Optimizador & Sensores",
                    subtitle = "Batería, RAM y Diagnósticos",
                    icon = Icons.Default.Sensors,
                    color = Color(0xFF00E676),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (activity != null) {
                            adMobManager.showInterstitialForFeature(
                                activity = activity,
                                adUnitId = AdMobUnits.INTERSTITIAL_OPTIMIZER,
                                featureName = "Optimizador & Sensores",
                                onDone = onNavigateToOptimizer
                            )
                        } else onNavigateToOptimizer()
                    }
                )
                QuickActionCard(
                    title = "Nube Multi-Dispositivo",
                    subtitle = "Copias y Sincronización en vivo",
                    icon = Icons.Default.CloudSync,
                    color = Color(0xFFD500F9),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (activity != null) {
                            adMobManager.showInterstitialForFeature(
                                activity = activity,
                                adUnitId = AdMobUnits.INTERSTITIAL_TAXOBETAPUB,
                                featureName = "Nube Multi-Dispositivo",
                                onDone = onNavigateToSync
                            )
                        } else onNavigateToSync()
                    }
                )
            }
        }

        item {
            val activity = context as? android.app.Activity
            QuickActionCard(
                title = "Limpiador de Basura",
                subtitle = "Eliminar Caché, Temporales y Fotos Duplicadas",
                icon = Icons.Default.DeleteSweep,
                color = Color(0xFFFF5252),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (activity != null) {
                        adMobManager.showInterstitialForFeature(
                            activity = activity,
                            adUnitId = AdMobUnits.INTERSTITIAL_OPTIMIZER,
                            featureName = "Limpiador de Basura & Duplicados",
                            onDone = onNavigateToOptimizer
                        )
                    } else onNavigateToOptimizer()
                }
            )
        }

        // Network telemetry tile
        item {
            CyberMetricTile(
                title = "Red & Conectividad",
                value = "${network.connectionType} • IP: ${network.ipAddress} (${network.latencyMs}ms)",
                icon = Icons.Default.NetworkCheck,
                accentColor = accent
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(
                1.dp,
                Brush.linearGradient(listOf(color.copy(alpha = 0.5f), BorderDim)),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
