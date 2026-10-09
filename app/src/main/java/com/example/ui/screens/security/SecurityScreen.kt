package com.example.ui.screens.security

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobManager
import com.example.ads.AdMobUnits
import com.example.security.AppSecurityReport
import com.example.security.CyberPermissionStatusCard
import com.example.security.CyberSecurityManager
import com.example.security.RiskLevel
import com.example.security.SystemIntegrityReport
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberGauge
import com.example.ui.components.CyberStatusPill
import com.example.ui.theme.BorderDim
import com.example.ui.theme.LocalAccentColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SecurityScreen(
    securityManager: CyberSecurityManager,
    adMobManager: AdMobManager,
    permissionsState: MultiplePermissionsState? = null,
    onOpenPermissions: () -> Unit = {},
    onNavigateToVault: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val accent = LocalAccentColor.current

    val magneticField by securityManager.magneticFieldState.collectAsState()
    val isShieldActive by securityManager.isShieldActive.collectAsState()

    var appsReport by remember { mutableStateOf<List<AppSecurityReport>>(emptyList()) }
    var integrityReport by remember { mutableStateOf<SystemIntegrityReport?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    var selectedFilter by remember { mutableStateOf("Inactivas con Cámara/Mic") }
    val filterTabs = listOf("Inactivas con Cámara/Mic", "Todas", "Riesgo Alto", "Cámara", "Micrófono")

    LaunchedEffect(Unit) {
        isScanning = true
        val report = securityManager.auditInstalledApps()
        appsReport = report
        integrityReport = securityManager.checkSystemIntegrity(report)
        isScanning = false
    }

    // High EMF alert (> 75 uT usually indicates hidden electronics/camera lens nearby)
    val isHighEmf = magneticField > 75f

    LaunchedEffect(isHighEmf) {
        if (isHighEmf) {
            try {
                val vibrator = context.getSystemService(Vibrator::class.java)
                vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } catch (ignored: Exception) {}
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Accompanist Permissions Runtime Status Card for Total Device Control
        if (permissionsState != null) {
            item {
                CyberPermissionStatusCard(
                    permissionsState = permissionsState,
                    onOpenHub = onOpenPermissions
                )
            }
        }

        // Blindaje Activo & Score de Seguridad
        item {
            CyberCard(glowColor = if (isShieldActive) Color(0xFF00E676) else Color(0xFFFF5252)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isShieldActive) Color(0x2200E676) else Color(0x22FF5252)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isShieldActive) Icons.Default.Shield else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isShieldActive) Color(0xFF00E676) else Color(0xFFFF5252),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isShieldActive) "PANEL CENTRAL DE SEGURIDAD BLINDADA" else "ESCUDO EN PAUSA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isShieldActive) Color(0xFF00E676) else Color(0xFFFF5252)
                                )
                                Text(
                                    text = "Score de Protección: ${integrityReport?.securityScore ?: 95}/100 • Auditoría Activa",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isShieldActive,
                            onCheckedChange = { enable ->
                                val activity = context as? android.app.Activity
                                if (enable && activity != null) {
                                    adMobManager.showInterstitialForFeature(
                                        activity = activity,
                                        adUnitId = AdMobUnits.INTERSTITIAL_SECURITY,
                                        featureName = "Blindaje Activo Militar",
                                        onDone = { securityManager.toggleShield(true) }
                                    )
                                } else {
                                    securityManager.toggleShield(enable)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E676))
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dashboard stats cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SecurityStatMini(
                            label = "Auditoría Apps",
                            value = "${integrityReport?.totalAppsAudited ?: appsReport.size}",
                            color = accent,
                            modifier = Modifier.weight(1f)
                        )
                        SecurityStatMini(
                            label = "Inactivas Latentes",
                            value = "${integrityReport?.dormantSensitiveAppsCount ?: appsReport.count { it.isDormantSensitive }}",
                            color = Color(0xFFFF1744),
                            modifier = Modifier.weight(1f)
                        )
                        SecurityStatMini(
                            label = "Alto Riesgo",
                            value = "${integrityReport?.highRiskAppsCount ?: appsReport.count { it.riskLevel == RiskLevel.HIGH }}",
                            color = Color(0xFFFF9100),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Bóveda Cifrada Local (Room) Shortcut
        if (onNavigateToVault != null) {
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x22D500F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFD500F9), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "BÓVEDA CIFRADA (ROOM)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD500F9)
                                )
                                Text(
                                    text = "Notas y credenciales seguras con desbloqueo biométrico",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Abrir")
                        }
                    }
                }
            }
        }

        // Detector de Cámaras Ocultas por Campo Magnético (EMF Scanner)
        item {
            CyberCard(glowColor = if (isHighEmf) Color(0xFFFF1744) else Color(0xFF00E5FF)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = if (isHighEmf) Color(0xFFFF1744) else Color(0xFF00E5FF))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DETECTOR DE CÁMARAS OCULTAS",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isHighEmf) Color(0xFFFF1744) else Color(0xFF00E5FF)
                            )
                        }
                        CyberStatusPill(
                            text = if (isHighEmf) "¡ANOMALÍA!" else "NORMAL",
                            isActive = isHighEmf,
                            activeColor = if (isHighEmf) Color(0xFFFF1744) else Color(0xFF00E676)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Acerca la parte trasera de tu teléfono a enchufes, marcos o espejos sospechosos. Lentes espía y transmisores emiten radiación electromagnética medible.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CyberGauge(
                            percentage = (magneticField / 1.5f).toInt().coerceIn(0, 100),
                            label = "${String.format("%.1f", magneticField)} µT",
                            size = 100.dp,
                            strokeWidth = 9.dp,
                            accentColor = if (isHighEmf) Color(0xFFFF1744) else Color(0xFF00E5FF)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHighEmf) "¡POSIBLE DISPOSITIVO ESPÍA CERCA!" else "Campo magnético ambiente seguro",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isHighEmf) Color(0xFFFF1744) else Color(0xFF00E676)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isHighEmf) "Radiación superó los 75 µT. Inspecciona el área minuciosamente." else "Valores normales: 30 a 55 µT.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Auditoría Central de Aplicaciones Instaladas
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "APLICACIONES CON ACCESO A CÁMARA & MICRÓFONO",
                            style = MaterialTheme.typography.titleMedium,
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Detección de aplicaciones inactivas con permisos sensibles latentes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            if (!isScanning) {
                                val activity = context as? android.app.Activity
                                val scanAction: () -> Unit = {
                                    isScanning = true
                                    scope.launch {
                                        val report = securityManager.auditInstalledApps()
                                        appsReport = report
                                        integrityReport = securityManager.checkSystemIntegrity(report)
                                        isScanning = false
                                    }
                                }
                                if (activity != null) {
                                    adMobManager.showInterstitialForFeature(
                                        activity = activity,
                                        adUnitId = AdMobUnits.INTERSTITIAL_SECURITY,
                                        featureName = "Auditoría de Seguridad y Detección de Espías",
                                        onDone = scanAction
                                    )
                                } else {
                                    scanAction()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = accent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = accent)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Escanear")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterTabs) { tab ->
                        val isSelected = selectedFilter == tab
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedFilter = tab },
                            color = if (isSelected) accent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) accent else BorderDim),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = tab,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        val filteredApps = when (selectedFilter) {
            "Inactivas con Cámara/Mic" -> appsReport.filter { it.isDormantSensitive }
            "Riesgo Alto" -> appsReport.filter { it.riskLevel == RiskLevel.HIGH }
            "Cámara" -> appsReport.filter { it.hasCameraAccess }
            "Micrófono" -> appsReport.filter { it.hasAudioAccess }
            else -> appsReport
        }

        if (filteredApps.isEmpty() && !isScanning) {
            item {
                CyberCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "No se encontraron aplicaciones en esta categoría.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(filteredApps, key = { it.packageName }) { app ->
                val badgeColor = when {
                    app.isDormantSensitive -> Color(0xFFFF1744)
                    app.riskLevel == RiskLevel.HIGH -> Color(0xFFFF1744)
                    app.riskLevel == RiskLevel.MEDIUM -> Color(0xFFFFAB00)
                    else -> Color(0xFF00E676)
                }

                CyberCard(glowColor = badgeColor) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    CyberStatusPill(
                                        text = if (app.isDormantSensitive) "INACTIVA LATENTE" else when (app.riskLevel) {
                                            RiskLevel.HIGH -> "RIESGO ALTO"
                                            RiskLevel.MEDIUM -> "ATENCIÓN"
                                            RiskLevel.LOW -> "CONFIABLE"
                                        },
                                        isActive = true,
                                        activeColor = badgeColor
                                    )
                                }

                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (app.hasCameraAccess) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Cámara", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF5252))
                                        }
                                    }
                                    if (app.hasAudioAccess) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFFFFAB00), modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Micrófono", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFAB00))
                                        }
                                    }
                                    if (app.hasLocationAccess) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("GPS", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00E5FF))
                                        }
                                    }
                                    if (app.daysInactive > 0) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF90CAF9), modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Inactiva: ${app.daysInactive}d", style = MaterialTheme.typography.labelSmall, color = Color(0xFF90CAF9))
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = { securityManager.openAppPermissionsSettings(app.packageName) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (app.isDormantSensitive) Color(0xFFFF1744) else accent
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Bloquear", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (app.securityWarning.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = if (app.isDormantSensitive) Color(0x33FF1744) else Color(0x22FFAB00),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (app.isDormantSensitive) Color(0x66FF1744) else Color(0x44FFAB00))
                            ) {
                                Text(
                                    text = app.securityWarning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (app.isDormantSensitive) Color(0xFFFF8A80) else Color(0xFFFFD54F),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityStatMini(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0x66080E1C),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDim)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
