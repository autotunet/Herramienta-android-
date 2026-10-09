package com.example.security

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberStatusPill
import com.example.ui.theme.BorderDim
import com.example.ui.theme.LocalAccentColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

object CyberAppPermissions {
    val REQUIRED_PERMISSIONS = buildList {
        add(Manifest.permission.CAMERA)
        add(Manifest.permission.RECORD_AUDIO)
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        add(Manifest.permission.READ_PHONE_STATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    data class PermissionDetail(
        val permission: String,
        val title: String,
        val subtitle: String,
        val icon: ImageVector
    )

    fun getDetailFor(permission: String): PermissionDetail {
        return when (permission) {
            Manifest.permission.CAMERA -> PermissionDetail(
                permission = permission,
                title = "Cámara & Sensores Ópticos",
                subtitle = "Inspección de hardware para detector de cámaras ocultas.",
                icon = Icons.Default.CameraAlt
            )
            Manifest.permission.RECORD_AUDIO -> PermissionDetail(
                permission = permission,
                title = "Micrófono & Escudo de Audio",
                subtitle = "Bloqueo y alerta contra escuchas espías en segundo plano.",
                icon = Icons.Default.Mic
            )
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION -> PermissionDetail(
                permission = permission,
                title = "Ubicación & Antenas de Red",
                subtitle = "Medición precisa de dBm, SSID y celdas Wi-Fi / Datos móviles.",
                icon = Icons.Default.MyLocation
            )
            Manifest.permission.READ_PHONE_STATE -> PermissionDetail(
                permission = permission,
                title = "Estado Telefónico & Red Móvil",
                subtitle = "Identificación de telemetría de operadoras (Claro, Movistar, etc.).",
                icon = Icons.Default.PhoneAndroid
            )
            Manifest.permission.POST_NOTIFICATIONS -> PermissionDetail(
                permission = permission,
                title = "Notificaciones de Seguridad",
                subtitle = "Alertas en vivo de microcortes, intrusiones y malware.",
                icon = Icons.Default.Notifications
            )
            else -> PermissionDetail(
                permission = permission,
                title = "Permiso de Sistema",
                subtitle = "Requerido para control total de herramientas.",
                icon = Icons.Default.Security
            )
        }
    }
}

/**
 * High-tech Cyber Permissions Dialog powered by Accompanist Permissions library.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CyberPermissionHubDialog(
    permissionsState: MultiplePermissionsState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val accent = LocalAccentColor.current

    val grantedCount = permissionsState.permissions.count { it.status.isGranted }
    val totalCount = permissionsState.permissions.size
    val progress = if (totalCount > 0) grantedCount.toFloat() / totalCount else 1f

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF070C18),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.2f))
                        .border(1.dp, accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "CONTROL TOTAL DEL DISPOSITIVO",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Protocolo de Permisos AndroSuite Pro",
                        color = accent,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Para activar la potencia máxima de red, blindaje anti-espía y herramientas militares, el sistema solicita autorización en las siguientes áreas:",
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nivel de Autorización: $grantedCount/$totalCount",
                        fontSize = 11.sp,
                        color = if (grantedCount == totalCount) Color(0xFF00E676) else accent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (grantedCount == totalCount) Color(0xFF00E676) else accent,
                    trackColor = Color(0x33FFFFFF)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Unique permission items
                val uniquePermissions = remember(permissionsState.permissions) {
                    // Filter duplicates like coarse location if fine location is present
                    val seen = mutableSetOf<String>()
                    permissionsState.permissions.filter {
                        val key = if (it.permission.contains("LOCATION")) "LOCATION" else it.permission
                        seen.add(key)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uniquePermissions.forEach { permState ->
                        val detail = CyberAppPermissions.getDetailFor(permState.permission)
                        val isGranted = permState.status.isGranted

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF0E1526),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isGranted) Color(0x4400E676) else BorderDim
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = detail.icon,
                                        contentDescription = null,
                                        tint = if (isGranted) Color(0xFF00E676) else accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = detail.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = detail.subtitle,
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                            lineHeight = 12.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                CyberStatusPill(
                                    text = if (isGranted) "CONCEDIDO" else "PENDIENTE",
                                    isActive = isGranted,
                                    activeColor = Color(0xFF00E676)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (permissionsState.shouldShowRationale) {
                        permissionsState.launchMultiplePermissionRequest()
                    } else if (!permissionsState.allPermissionsGranted) {
                        // Launch request directly via Accompanist
                        permissionsState.launchMultiplePermissionRequest()
                    } else {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (permissionsState.allPermissionsGranted) "LISTO" else "CONCEDER PERMISOS AHORA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    if (!permissionsState.allPermissionsGranted && permissionsState.shouldShowRationale) {
                        // Open Android Settings
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else {
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (!permissionsState.allPermissionsGranted && permissionsState.shouldShowRationale) "AJUSTES" else "IGNORAR",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    )
}

/**
 * Reusable banner card for in-screen display (Dashboard & Security screens)
 * using Accompanist permissions state.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CyberPermissionStatusCard(
    permissionsState: MultiplePermissionsState,
    modifier: Modifier = Modifier,
    onOpenHub: () -> Unit
) {
    val accent = LocalAccentColor.current
    val grantedCount = permissionsState.permissions.count { it.status.isGranted }
    val totalCount = permissionsState.permissions.size
    val allGranted = permissionsState.allPermissionsGranted

    CyberCard(
        modifier = modifier,
        glowColor = if (allGranted) Color(0xFF00E676) else Color(0xFFFF9100)
    ) {
        Column {
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
                            .background(if (allGranted) Color(0x2200E676) else Color(0x22FF9100)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (allGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (allGranted) Color(0xFF00E676) else Color(0xFFFF9100),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (allGranted) "CONTROL TOTAL ACTIVO (100%)" else "PERMISOS DE CONTROL PENDIENTES",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (allGranted) Color(0xFF00E676) else Color(0xFFFF9100)
                        )
                        Text(
                            text = if (allGranted) "Todas las herramientas operan al 100% de potencia" else "Faltan permisos para activar todas las herramientas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = {
                        if (!allGranted) {
                            permissionsState.launchMultiplePermissionRequest()
                        } else {
                            onOpenHub()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allGranted) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFFF9100),
                        contentColor = if (allGranted) accent else Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (allGranted) "Verificar" else "Autorizar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Permissions mini tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val camGranted = permissionsState.permissions.any { it.permission == Manifest.permission.CAMERA && it.status.isGranted }
                val micGranted = permissionsState.permissions.any { it.permission == Manifest.permission.RECORD_AUDIO && it.status.isGranted }
                val locGranted = permissionsState.permissions.any { it.permission == Manifest.permission.ACCESS_FINE_LOCATION && it.status.isGranted }
                val phoneGranted = permissionsState.permissions.any { it.permission == Manifest.permission.READ_PHONE_STATE && it.status.isGranted }

                PermissionMiniBadge(label = "CÁMARA", isGranted = camGranted, modifier = Modifier.weight(1f))
                PermissionMiniBadge(label = "MIC", isGranted = micGranted, modifier = Modifier.weight(1f))
                PermissionMiniBadge(label = "ANTENAS", isGranted = locGranted, modifier = Modifier.weight(1f))
                PermissionMiniBadge(label = "RED", isGranted = phoneGranted, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PermissionMiniBadge(
    label: String,
    isGranted: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isGranted) Color(0x2200E676) else Color(0x22FF5252))
            .border(
                1.dp,
                if (isGranted) Color(0x4400E676) else Color(0x44FF5252),
                RoundedCornerShape(6.dp)
            )
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$label ${if (isGranted) "✓" else "✗"}",
            color = if (isGranted) Color(0xFF00E676) else Color(0xFFFF5252),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
