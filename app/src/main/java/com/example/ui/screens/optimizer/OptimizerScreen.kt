package com.example.ui.screens.optimizer

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobManager
import com.example.ads.AdMobUnits
import com.example.telemetry.JunkCleanerManager
import com.example.telemetry.NetworkSpeedTestManager
import com.example.telemetry.SignalBoosterManager
import com.example.telemetry.SystemTelemetryManager
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberGauge
import com.example.ui.components.CyberJunkCleanerCard
import com.example.ui.components.CyberMetricTile
import com.example.ui.components.CyberSpeedTestCard
import com.example.ui.components.CyberStatusPill
import com.example.ui.theme.BorderDim
import com.example.ui.theme.LocalAccentColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OptimizerScreen(
    telemetryManager: SystemTelemetryManager,
    signalBooster: SignalBoosterManager,
    adMobManager: AdMobManager,
    speedTestManager: NetworkSpeedTestManager? = null,
    junkCleanerManager: JunkCleanerManager? = null
) {
    val context = LocalContext.current
    val actualSpeedTestManager = speedTestManager ?: remember { NetworkSpeedTestManager(context) }
    val actualJunkCleanerManager = junkCleanerManager ?: remember { JunkCleanerManager(context) }
    val battery by telemetryManager.batteryState.collectAsState()
    val memory by telemetryManager.memoryState.collectAsState()
    val storage by telemetryManager.storageState.collectAsState()
    val network by telemetryManager.networkState.collectAsState()
    val sensor by telemetryManager.sensorState.collectAsState()
    val signal by signalBooster.signalState.collectAsState()

    val scope = rememberCoroutineScope()
    val accent = LocalAccentColor.current

    var isOptimizing by remember { mutableStateOf(false) }
    var optimizationResult by remember { mutableStateOf<String?>(null) }
    var isBoostingSignal by remember { mutableStateOf(false) }
    var signalBoostResult by remember { mutableStateOf<String?>(null) }
    var isTestingPing by remember { mutableStateOf(false) }
    var pingResult by remember { mutableStateOf<String?>(null) }
    var isStabilizing by remember { mutableStateOf(false) }
    var stabilizationResult by remember { mutableStateOf<String?>(null) }

    // Touch diagnostic points
    val touchPoints = remember { mutableStateListOf<Offset>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // RAM & Process Deep Cleaner
        item {
            CyberCard(glowColor = accent) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "OPTIMIZADOR DE MEMORIA RAM", style = MaterialTheme.typography.titleMedium, color = accent)
                            Text(
                                text = "Uso actual: ${memory.usedPercent}% • ${memory.usedRamMb} MB ocupados",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                if (!isOptimizing) {
                                    val activity = context as? android.app.Activity
                                    val action: () -> Unit = {
                                        isOptimizing = true
                                        scope.launch {
                                            delay(500)
                                            val freed = telemetryManager.performRamClean()
                                            try {
                                                val vibrator = context.getSystemService(Vibrator::class.java)
                                                vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                                            } catch (ignored: Exception) {}
                                            optimizationResult = "¡Excelente! Se liberaron ${freed} MB y se cerraron procesos en segundo plano."
                                            isOptimizing = false
                                        }
                                    }
                                    if (activity != null) {
                                        adMobManager.showInterstitialForFeature(
                                            activity = activity,
                                            adUnitId = AdMobUnits.INTERSTITIAL_OPTIMIZER,
                                            featureName = "Limpiador Profundo de RAM",
                                            onDone = action
                                        )
                                    } else {
                                        action()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isOptimizing
                        ) {
                            if (isOptimizing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.Black)
                            } else {
                                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Limpiar")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { memory.usedPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (memory.usedPercent > 80) Color(0xFFFF5252) else accent,
                        trackColor = Color(0x22FFFFFF)
                    )

                    AnimatedVisibility(visible = optimizationResult != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            color = Color(0x2200E676),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6600E676))
                        ) {
                            Text(
                                text = optimizationResult ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF00E676),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Limpiador de Archivos Basura, Caché y Fotos Duplicadas
        item {
            CyberJunkCleanerCard(
                junkCleanerManager = actualJunkCleanerManager,
                adMobManager = adMobManager
            )
        }

        // Battery Health Diagnostics
        item {
            CyberCard(glowColor = Color(0xFF00E676)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = Color(0xFF00E676))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "DOCTOR DE BATERÍA", style = MaterialTheme.typography.titleMedium, color = Color(0xFF00E676))
                        }
                        CyberStatusPill(
                            text = battery.chargeType,
                            isActive = battery.isCharging,
                            activeColor = Color(0xFF00E676)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryMiniCard(label = "Nivel", value = "${battery.levelPercent}%", color = Color(0xFF00E676))
                        TelemetryMiniCard(label = "Temperatura", value = "${battery.temperatureCelsius}°C", color = if (battery.temperatureCelsius > 40) Color(0xFFFF5252) else accent)
                        TelemetryMiniCard(label = "Voltaje", value = "${battery.voltageMv} mV", color = Color(0xFFFFAB00))
                        TelemetryMiniCard(label = "Salud", value = battery.healthStatus, color = Color(0xFF00E676))
                    }
                }
            }
        }

        // Storage Analyzer Breakdown
        item {
            CyberCard(glowColor = Color(0xFFD500F9)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFFD500F9))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "ANALIZADOR DE ESPACIO", style = MaterialTheme.typography.titleMedium, color = Color(0xFFD500F9))
                        }
                        Text(
                            text = "${storage.freeGb} GB disponibles",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { storage.usedPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFFD500F9),
                        trackColor = Color(0x22FFFFFF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total: ${storage.totalGb} GB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Usado: ${storage.usedGb} GB (${storage.usedPercent}%)", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD500F9))
                    }
                }
            }
        }

        // Real-Time Hardware Sensor Diagnostics
        item {
            CyberCard(glowColor = accent) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = accent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "SENSORES EN TIEMPO REAL", style = MaterialTheme.typography.titleMedium, color = accent)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Acelerómetro (Giroscopio & Inclinación):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    SensorAxisBar(axis = "Eje X", value = sensor.accelX, color = Color(0xFF00E5FF))
                    Spacer(modifier = Modifier.height(4.dp))
                    SensorAxisBar(axis = "Eje Y", value = sensor.accelY, color = Color(0xFF00E676))
                    Spacer(modifier = Modifier.height(4.dp))
                    SensorAxisBar(axis = "Eje Z", value = sensor.accelZ, color = Color(0xFFD500F9))

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sensor de Luz Ambiental: ${String.format("%.1f", sensor.ambientLux)} Lux",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFAB00)
                    )
                }
            }
        }

        // Real Network Speed Test (Upload & Download with Gauge & Progress)
        item {
            CyberSpeedTestCard(
                speedTestManager = actualSpeedTestManager,
                adMobManager = adMobManager
            )
        }

        // Amplificador de Potencia Wi-Fi & Señal de Datos Multi-Compañía
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
                            Text(
                                text = "POTENCIADOR WI-FI & DATOS",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF00E5FF)
                            )
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
                                                vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
                                            } catch (ignored: Exception) {}
                                            signalBoostResult = "¡Potencia aumentada al ${boosted.signalStrengthPercent}%! DNS optimizado y latencia reducida a ${boosted.currentPingMs} ms."
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
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isBoostingSignal
                        ) {
                            if (isBoostingSignal) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                            } else {
                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Amplificar")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Compatible con todas las operadoras (Claro, Movistar, T-Mobile, AT&T, Telcel, Orange, Vodafone). Purga caché DNS, alinea paquetes MTU y prioriza canales de alta potencia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryMiniCard(label = "Operador", value = signal.carrierName.take(10), color = Color(0xFF00E5FF))
                        TelemetryMiniCard(label = "Potencia", value = "${signal.signalStrengthPercent}%", color = Color(0xFF00E676))
                        TelemetryMiniCard(label = "dBm", value = "${signal.signalDbm}", color = Color(0xFFFFAB00))
                        TelemetryMiniCard(label = "Ping", value = "${signal.currentPingMs}ms", color = Color(0xFFD500F9))
                    }

                    AnimatedVisibility(visible = signalBoostResult != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            color = Color(0x2200E676),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6600E676))
                        ) {
                            Text(
                                text = signalBoostResult ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF00E676),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Estabilizador de Red & Prioridad de Tráfico para Apps en Primer Plano
        item {
            CyberCard(glowColor = Color(0xFF00E676)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF00E676))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ESTABILIZADOR DE RED & QOS",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676)
                                )
                                Text(
                                    text = if (signal.foregroundPriorityActive) "Prioridad Foreground: ACTIVA • 0.0% Pérdida" else "Purga DNS y prioriza datos de primer plano",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (!isStabilizing) {
                                    val activity = context as? android.app.Activity
                                    val action: () -> Unit = {
                                        isStabilizing = true
                                        scope.launch {
                                            val stabilized = signalBooster.stabilizeNetworkAndPrioritizeForeground()
                                            try {
                                                val vibrator = context.getSystemService(Vibrator::class.java)
                                                vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                                            } catch (ignored: Exception) {}
                                            stabilizationResult = stabilized.lastStabilizationReport
                                            isStabilizing = false
                                        }
                                    }
                                    if (activity != null) {
                                        adMobManager.showInterstitialForFeature(
                                            activity = activity,
                                            adUnitId = AdMobUnits.INTERSTITIAL_OPTIMIZER,
                                            featureName = "Estabilizador de Red & Prioridad",
                                            onDone = action
                                        )
                                    } else {
                                        action()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isStabilizing
                        ) {
                            if (isStabilizing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                            } else {
                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Estabilizar")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Limpia la memoria caché de sockets DNS del sistema operativo y asigna prioridad de enrutamiento QoS a los paquetes de datos de la app en primer plano, evitando microcortes y caídas de señal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryMiniCard(label = "Estado DNS", value = if (signal.isStabilized) "Purga OK" else "Estándar", color = Color(0xFF00E676))
                        TelemetryMiniCard(label = "Pérdida", value = "${signal.packetLossPercent}%", color = Color(0xFF00E5FF))
                        TelemetryMiniCard(label = "Jitter", value = "${signal.jitterMs}ms", color = Color(0xFFFFAB00))
                        TelemetryMiniCard(label = "QoS App", value = if (signal.foregroundPriorityActive) "Prioridad" else "Normal", color = Color(0xFFD500F9))
                    }

                    AnimatedVisibility(visible = stabilizationResult != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            color = Color(0x2200E676),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6600E676))
                        ) {
                            Text(
                                text = stabilizationResult ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF00E676),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Super Turbo VIP 100% Signal - Rewarded Ad (Prestamevip)
        item {
            val activity = context as? android.app.Activity
            val vipUnlocked by adMobManager.vipUnlocked.collectAsState()
            var rewardMsg by remember { mutableStateOf<String?>(null) }

            CyberCard(glowColor = Color(0xFFFFD700)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFFFD700))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "SUPER TURBO VIP 100% POTENCIA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                Text(
                                    text = "Desbloquea el ancho de banda ilimitado con anuncio recompensado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (activity != null) {
                                    adMobManager.showRewardedForVip(
                                        activity = activity,
                                        rewardTitle = "Super Turbo Señal 100%",
                                        onRewardGranted = {
                                            scope.launch {
                                                signalBooster.boostSignal()
                                                rewardMsg = "¡FELICIDADES! Recompensa VIP Activada: Potencia fijada en 100% y Prioridad de canal máxima."
                                            }
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (vipUnlocked) "RE-ACTIVAR VIP" else "DESBLOQUEAR VIP", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Patrocinado por Prestamevip (${AdMobUnits.REWARDED_PRESTAMEVIP}). Mira un anuncio en video para recibir 100% de señal de datos continua sin restricciones.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AnimatedVisibility(visible = rewardMsg != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            color = Color(0x33FFD700),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Text(
                                text = rewardMsg ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFFD700),
                                modifier = Modifier.padding(8.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Screen & Touch Tester Canvas
        item {
            CyberCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, tint = accent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "TESTER DE PANTALLA & MULTITOUCH", style = MaterialTheme.typography.titleMedium, color = accent)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Toca el panel para verificar la precisión de coordenadas táctiles del digitalizador.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF080D1A))
                            .border(1.dp, BorderDim, RoundedCornerShape(12.dp))
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    if (touchPoints.size > 15) touchPoints.removeAt(0)
                                    touchPoints.add(offset)
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            touchPoints.forEach { point ->
                                drawCircle(
                                    color = Color(0xFF00E5FF),
                                    radius = 16.dp.toPx(),
                                    center = point
                                )
                                drawCircle(
                                    color = Color(0xFFD500F9),
                                    radius = 28.dp.toPx(),
                                    center = point,
                                    alpha = 0.3f
                                )
                            }
                        }

                        if (touchPoints.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Toca aquí para probar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            val last = touchPoints.last()
                            Text(
                                text = "X: ${last.x.toInt()}  Y: ${last.y.toInt()} (${touchPoints.size} toques)",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryMiniCard(label: String, value: String, color: Color) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDim)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun SensorAxisBar(axis: String, value: Float, color: Color) {
    val normalized = ((value + 15f) / 30f).coerceIn(0f, 1f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = axis, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(42.dp))
        LinearProgressIndicator(
            progress = { normalized },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color(0x22FFFFFF)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = String.format("%.2f", value),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(46.dp)
        )
    }
}
