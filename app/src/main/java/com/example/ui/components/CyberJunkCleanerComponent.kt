package com.example.ui.components

import android.app.Activity
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ads.AdMobManager
import com.example.ads.AdMobUnits
import com.example.i18n.CyberStrings
import com.example.i18n.LocalAppLanguage
import com.example.telemetry.JunkCategory
import com.example.telemetry.JunkCleanerManager
import com.example.telemetry.JunkItem
import com.example.ui.theme.BorderDim
import com.example.ui.theme.LocalAccentColor
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CyberJunkCleanerCard(
    junkCleanerManager: JunkCleanerManager,
    adMobManager: AdMobManager,
    modifier: Modifier = Modifier
) {
    val state by junkCleanerManager.state.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val accent = LocalAccentColor.current
    val lang = LocalAppLanguage.current

    var showCleanConfirmDialog by remember { mutableStateOf(false) }
    var cleanSuccessMsg by remember { mutableStateOf<String?>(null) }

    CyberCard(
        modifier = modifier,
        glowColor = accent
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.15f))
                            .border(1.dp, accent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = CyberStrings.junkCleanerTitle(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                        Text(
                            text = if (state.items.isEmpty() && !state.lastScanCompleted)
                                "Caché, archivos temporales y fotos duplicadas"
                            else
                                "${state.items.size} archivos encontrados • ${JunkItem.formatBytes(state.totalJunkBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Scan Button
                IconButton(
                    onClick = {
                        val activity = context as? Activity
                        val scanAction: () -> Unit = {
                            cleanSuccessMsg = null
                            scope.launch {
                                junkCleanerManager.scanJunk()
                            }
                        }
                        if (activity != null) {
                            adMobManager.showInterstitialForFeature(
                                activity = activity,
                                adUnitId = AdMobUnits.INTERSTITIAL_OPTIMIZER,
                                featureName = CyberStrings.scanStorage(lang),
                                onDone = scanAction
                            )
                        } else {
                            scanAction()
                        }
                    },
                    enabled = !state.isScanning && !state.isCleaning
                ) {
                    if (state.isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = accent
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = CyberStrings.scanStorage(lang),
                            tint = accent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Space Overview Dashboard
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF070B14),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDim)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cache Stat
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Caché", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = JunkItem.formatBytes(state.cacheBytes),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(BorderDim))

                    // Temp Stat
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FolderZip, contentDescription = null, tint = Color(0xFFFFAB00), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Temporales", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = JunkItem.formatBytes(state.tempBytes),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFAB00)
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(BorderDim))

                    // Duplicate Stat
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Collections, contentDescription = null, tint = Color(0xFFD500F9), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Duplicadas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = JunkItem.formatBytes(state.duplicateBytes),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD500F9)
                        )
                    }
                }
            }

            // Scanning Progress State
            AnimatedVisibility(visible = state.isScanning) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = state.currentScannedPath,
                            style = MaterialTheme.typography.bodySmall,
                            color = accent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${(state.scanProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { state.scanProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = accent,
                        trackColor = Color(0x33FFFFFF)
                    )
                }
            }

            // Success feedback message
            AnimatedVisibility(visible = cleanSuccessMsg != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    color = Color(0x2200E676),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x8800E676))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = cleanSuccessMsg ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedCategory == null,
                    onClick = { junkCleanerManager.filterByCategory(null) },
                    label = { Text("Todos (${state.items.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accent.copy(alpha = 0.25f),
                        selectedLabelColor = accent
                    )
                )

                FilterChip(
                    selected = state.selectedCategory == JunkCategory.CACHE,
                    onClick = { junkCleanerManager.filterByCategory(JunkCategory.CACHE) },
                    label = { Text("Caché", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0x3300E5FF),
                        selectedLabelColor = Color(0xFF00E5FF)
                    )
                )

                FilterChip(
                    selected = state.selectedCategory == JunkCategory.TEMPORARY,
                    onClick = { junkCleanerManager.filterByCategory(JunkCategory.TEMPORARY) },
                    label = { Text("Temporales", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0x33FFAB00),
                        selectedLabelColor = Color(0xFFFFAB00)
                    )
                )

                FilterChip(
                    selected = state.selectedCategory == JunkCategory.DUPLICATE_IMAGE,
                    onClick = { junkCleanerManager.filterByCategory(JunkCategory.DUPLICATE_IMAGE) },
                    label = { Text("Duplicadas", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0x33D500F9),
                        selectedLabelColor = Color(0xFFD500F9)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Select All / Deselect All Row
            val filteredItems = remember(state.items, state.selectedCategory) {
                if (state.selectedCategory == null) state.items
                else state.items.filter { it.category == state.selectedCategory }
            }

            if (filteredItems.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seleccionados: ${filteredItems.count { it.isSelected }} de ${filteredItems.size}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row {
                        Text(
                            text = "Todo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { junkCleanerManager.selectAll(true, state.selectedCategory) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ninguno",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { junkCleanerManager.selectAll(false, state.selectedCategory) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // List of items (limited height / scrollable column or items list)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Render top 15 items in card, or all
                    filteredItems.take(12).forEach { item ->
                        JunkItemRow(
                            item = item,
                            accentColor = accent,
                            onToggleSelect = { junkCleanerManager.toggleItemSelection(item.id) },
                            onDeleteSingle = {
                                scope.launch {
                                    val file = item.fileRef ?: File(item.path)
                                    if (file.exists()) file.delete()
                                    junkCleanerManager.scanJunk()
                                }
                            }
                        )
                    }

                    if (filteredItems.size > 12) {
                        Text(
                            text = "+ ${filteredItems.size - 12} archivos adicionales listos para limpieza",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 4.dp)
                        )
                    }
                }
            } else if (!state.isScanning) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = accent.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (state.lastScanCompleted)
                                "¡Almacenamiento impecable! No hay archivos basura."
                            else
                                "Presiona el botón para escanear archivos basura y duplicados.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Scan & Clean
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val activity = context as? Activity
                        val scanAction: () -> Unit = {
                            cleanSuccessMsg = null
                            scope.launch {
                                junkCleanerManager.scanJunk()
                            }
                        }
                        if (activity != null) {
                            adMobManager.showInterstitialForFeature(
                                activity = activity,
                                adUnitId = AdMobUnits.INTERSTITIAL_OPTIMIZER,
                                featureName = CyberStrings.scanStorage(lang),
                                onDone = scanAction
                            )
                        } else {
                            scanAction()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = accent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isScanning && !state.isCleaning
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(CyberStrings.scanStorage(lang), fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        showCleanConfirmDialog = true
                    },
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = state.selectedCount > 0 && !state.isCleaning && !state.isScanning
                ) {
                    if (state.isCleaning) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Limpiando...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Liberar ${JunkItem.formatBytes(state.selectedJunkBytes)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Clean Confirmation Dialog
    if (showCleanConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCleanConfirmDialog = false },
            containerColor = Color(0xFF090E1A),
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Confirmar Limpieza de Basura",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Se eliminarán permanentemente ${state.selectedCount} archivos seleccionados (${JunkItem.formatBytes(state.selectedJunkBytes)}).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Caché y temporales se regenerarán cuando sea necesario.\n• Para imágenes duplicadas, la copia original se preserva intacta.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF00E676)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCleanConfirmDialog = false
                        val activity = context as? Activity
                        val cleanAction: () -> Unit = {
                            scope.launch {
                                val freed = junkCleanerManager.cleanSelectedJunk()
                                try {
                                    val vibrator = context.getSystemService(Vibrator::class.java)
                                    vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
                                } catch (ignored: Exception) {}
                                cleanSuccessMsg = "¡Limpieza completada! Se liberaron ${JunkItem.formatBytes(freed)} de almacenamiento."
                            }
                        }
                        if (activity != null) {
                            adMobManager.showInterstitialForFeature(
                                activity = activity,
                                adUnitId = AdMobUnits.INTERSTITIAL_OPTIMIZER,
                                featureName = CyberStrings.cleanSelected(lang),
                                onDone = cleanAction
                            )
                        } else {
                            cleanAction()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Eliminar y Liberar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCleanConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun JunkItemRow(
    item: JunkItem,
    accentColor: Color,
    onToggleSelect: () -> Unit,
    onDeleteSingle: () -> Unit
) {
    val categoryColor = when (item.category) {
        JunkCategory.CACHE -> Color(0xFF00E5FF)
        JunkCategory.TEMPORARY -> Color(0xFFFFAB00)
        JunkCategory.DUPLICATE_IMAGE -> Color(0xFFD500F9)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelect() },
        color = Color(0x66070B14),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isSelected) categoryColor.copy(alpha = 0.5f) else BorderDim
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = item.isSelected,
                onCheckedChange = { onToggleSelect() },
                colors = CheckboxDefaults.colors(
                    checkedColor = categoryColor,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Thumbnail for duplicate images or Category Icon
            if (item.category == JunkCategory.DUPLICATE_IMAGE && item.fileRef?.exists() == true) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.fileRef)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, categoryColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(categoryColor.copy(alpha = 0.15f))
                        .border(1.dp, categoryColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (item.category) {
                            JunkCategory.CACHE -> Icons.Default.Storage
                            JunkCategory.TEMPORARY -> Icons.Default.FolderZip
                            JunkCategory.DUPLICATE_IMAGE -> Icons.Default.Collections
                        },
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Name and path details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Tag if duplicate copy or original
                    if (item.category == JunkCategory.DUPLICATE_IMAGE) {
                        Surface(
                            color = if (item.isDuplicateCopy) Color(0x33FF5252) else Color(0x3300E676),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = if (item.isDuplicateCopy) "Copia" else "Original",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isDuplicateCopy) Color(0xFFFF5252) else Color(0xFF00E676),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.formattedSize,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )

                    Text(
                        text = item.formattedDate,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Trash action
            IconButton(
                onClick = onDeleteSingle,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
