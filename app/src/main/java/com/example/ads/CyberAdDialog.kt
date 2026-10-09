package com.example.ads

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderDim
import com.example.ui.theme.LocalAccentColor

/**
 * Global Host for Pending Ad Interstitials and Sponsored Prompts.
 * Automatically shows whenever any feature activation triggers an Ad prompt.
 */
@Composable
fun CyberGlobalAdPromptHost(adMobManager: AdMobManager) {
    val prompt by adMobManager.pendingPrompt.collectAsState()

    if (prompt != null) {
        val currentPrompt = prompt!!
        CyberAdPromptDialog(
            featureTitle = currentPrompt.featureName,
            adUnitId = currentPrompt.adUnitId,
            isRewardedVip = currentPrompt.isRewarded,
            onWatchAd = {
                currentPrompt.onConfirm()
            },
            onDismiss = {
                adMobManager.dismissPendingPrompt()
            }
        )
    }
}

/**
 * High-Impact Cyber Ad Interstitial / Sponsored Authorization Dialog
 */
@Composable
fun CyberAdPromptDialog(
    featureTitle: String,
    onWatchAd: () -> Unit,
    onDismiss: () -> Unit,
    adUnitId: String = AdMobUnits.INTERSTITIAL_TAXIGORD_V1,
    isRewardedVip: Boolean = false
) {
    val accent = LocalAccentColor.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A0F1D),
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
                        imageVector = if (isRewardedVip) Icons.Default.CardGiftcard else Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isRewardedVip) "DESBLOQUEO VIP RECOMPENSADO" else "AUTORIZACIÓN PATROCINADA ADMOB",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Google AdMob • Red Militar Activa",
                        color = accent,
                        fontSize = 10.sp
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    text = "Para activar la función \"$featureTitle\", el sistema requiere verificar el anuncio publicitario patrocinador.",
                    color = Color(0xFFDDDDDD),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F182B))
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(BorderDim, accent.copy(alpha = 0.5f))),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRewardedVip)
                                    "ANUNCIO RECOMPENSADO VIP ACTIVO"
                                else
                                    "ANUNCIO INTERSTICIAL EMERGENTE",
                                color = accent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Unidad ID: $adUnitId",
                            color = Color(0xFF88A0C0),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRewardedVip)
                                "🎁 Otorga desbloqueo total sin límites y velocidad turbo militar."
                            else
                                "⚡ Mantiene AndroSuite Pro 100% gratuito y con tecnología en tiempo real.",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onWatchAd,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("VER ANUNCIO & ACTIVAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("CANCELAR", color = Color.Gray, fontSize = 12.sp)
            }
        }
    )
}

/**
 * Floating Interactive Cyber Ad Widget.
 * Can be docked floating over content to give quick access to VIP rewarded ads,
 * sponsored interstitials, and view current AdMob status.
 */
@Composable
fun CyberFloatingAdWidget(
    adMobManager: AdMobManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val adsCount by adMobManager.adsShownCount.collectAsState()
    val vipUnlocked by adMobManager.vipUnlocked.collectAsState()
    val accent = LocalAccentColor.current
    var isExpanded by remember { mutableStateOf(false) }
    var showVipModal by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .padding(end = 12.dp, bottom = 6.dp)
    ) {
        if (!isExpanded) {
            // Compact Floating Pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { isExpanded = true }
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(Color(0xFFFFD700), accent)),
                        RoundedCornerShape(20.dp)
                    ),
                color = Color(0xF00A1020),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (vipUnlocked) Color(0xFFFFD700) else accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (vipUnlocked) Icons.Default.Star else Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (vipUnlocked) "VIP ACTIVO" else "ANUNCIO VIP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (vipUnlocked) Color(0xFFFFD700) else Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "$adsCount",
                            fontSize = 9.sp,
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Expanded Floating Ad Card
            Surface(
                modifier = Modifier
                    .width(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(Color(0xFFFFD700), accent, BorderDim)),
                        RoundedCornerShape(16.dp)
                    ),
                color = Color(0xF80A1024),
                shadowElevation = 12.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CENTRAL DE ANUNCIOS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        IconButton(
                            onClick = { isExpanded = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "8 Unidades AdMob integradas. Mira anuncios para desbloquear potencia VIP militar o patrocinar herramientas.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rewarded ad trigger button
                    Button(
                        onClick = {
                            isExpanded = false
                            if (activity != null) {
                                adMobManager.showRewardedForVip(
                                    activity = activity,
                                    rewardTitle = "Modo Turbo VIP",
                                    onRewardGranted = {
                                        showVipModal = true
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ver Recompensado VIP (8970...)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Interstitial ad trigger button
                    OutlinedButton(
                        onClick = {
                            isExpanded = false
                            if (activity != null) {
                                adMobManager.showInterstitialForFeature(
                                    activity = activity,
                                    adUnitId = AdMobUnits.INTERSTITIAL_TAXIGORD_V1,
                                    featureName = "Patrocinador Flotante Taxigord v1",
                                    onDone = {}
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ver Intersticial (2803...)", fontSize = 11.sp, color = accent)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Anuncios vistos: $adsCount • Banner inferior activo",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    if (showVipModal) {
        AlertDialog(
            onDismissRequest = { showVipModal = false },
            containerColor = Color(0xFF0F1B2B),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¡SUPER TURBO VIP ACTIVADO!", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    text = "¡Excelente! Has completado el anuncio patrocinado de Prestamevip. Tu dispositivo cuenta ahora con potencia de señal reforzada y acceso sin límites en todas las funciones.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showVipModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black)
                ) {
                    Text("ACEPTAR", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
