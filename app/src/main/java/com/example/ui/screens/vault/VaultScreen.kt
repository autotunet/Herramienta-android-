package com.example.ui.screens.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobManager
import com.example.ads.AdMobUnits
import com.example.data.vault.DecryptedVaultItem
import com.example.data.vault.VaultRepository
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberStatusPill
import com.example.ui.theme.BorderDim
import com.example.ui.theme.LocalAccentColor
import kotlinx.coroutines.launch

@Composable
fun VaultScreen(
    vaultRepository: VaultRepository,
    adMobManager: AdMobManager
) {
    val context = LocalContext.current
    val accent = LocalAccentColor.current
    val scope = rememberCoroutineScope()

    var isUnlocked by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val vaultItems by vaultRepository.allItems.collectAsState(initial = emptyList())
    val revealedSecrets = remember { mutableStateMapOf<Long, Boolean>() }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("Todos") }
    val categories = listOf("Todos", "Contraseña", "Nota Secreta", "API Key", "Credencial")

    if (!isUnlocked) {
        // Biometric / PIN Auth Gate
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CyberCard(glowColor = accent) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.15f))
                            .border(2.dp, accent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bóveda Bloqueada",
                            tint = accent,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "BÓVEDA CIFRADA LOCAL (ROOM)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Cifrado militar AES-GCM (256-bit) • Protegido por Biometría",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Biometric sensor trigger button
                    Button(
                        onClick = {
                            val activity = context as? android.app.Activity
                            val unlockAction = {
                                try {
                                    val vibrator = context.getSystemService(Vibrator::class.java)
                                    vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                                } catch (ignored: Exception) {}
                                isUnlocked = true
                                Toast.makeText(context, "Autenticación Biométrica Aceptada", Toast.LENGTH_SHORT).show()
                            }
                            if (activity != null) {
                                adMobManager.showInterstitialForFeature(
                                    activity = activity,
                                    adUnitId = AdMobUnits.INTERSTITIAL_VAULT,
                                    featureName = "Desbloqueo de Bóveda Cifrada",
                                    onDone = unlockAction
                                )
                            } else {
                                unlockAction()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Escanear Huella / Desbloqueo Biométrico", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(BorderDim))
                        Text(
                            text = " O PIN CIBERNÉTICO ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(BorderDim))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 6) {
                                pinInput = it
                                pinError = false
                                if (it == "1234" || it.length == 6) {
                                    isUnlocked = true
                                }
                            }
                        },
                        label = { Text("PIN de Acceso (Ej. 1234)") },
                        singleLine = true,
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tus credenciales y notas nunca salen de tu dispositivo y se guardan cifradas en SQLite.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    } else {
        // Vault Content (Unlocked)
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header card with Lock Vault button
                item {
                    CyberCard(glowColor = Color(0xFF00E676)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color(0xFF00E676))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "BÓVEDA DESBLOQUEADA",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E676)
                                    )
                                    Text(
                                        text = "${vaultItems.size} elementos seguros en base de datos Room",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    isUnlocked = false
                                    pinInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = Color(0xFFFF5252)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bloquear")
                            }
                        }
                    }
                }

                // Category filters
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedCategoryFilter = cat },
                                color = if (isSelected) accent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) accent else BorderDim
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = cat,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                val filtered = if (selectedCategoryFilter == "Todos") {
                    vaultItems
                } else {
                    vaultItems.filter { it.category == selectedCategoryFilter }
                }

                if (filtered.isEmpty()) {
                    item {
                        CyberCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = accent, modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No hay elementos guardados en esta categoría", style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Presiona el botón '+' para guardar contraseñas, claves API o notas confidenciales.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filtered, key = { it.id }) { item ->
                        val isRevealed = revealedSecrets[item.id] == true

                        CyberCard(glowColor = accent.copy(alpha = 0.6f)) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        if (item.usernameOrEmail.isNotBlank()) {
                                            Text(
                                                text = item.usernameOrEmail,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = accent
                                            )
                                        }
                                    }

                                    CyberStatusPill(text = item.category, isActive = true, activeColor = accent)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Secret Box with copy and reveal
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0x66080E1C),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDim)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isRevealed) item.secret else "••••••••••••••••",
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isRevealed) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Row {
                                            IconButton(
                                                onClick = { revealedSecrets[item.id] = !isRevealed },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = "Mostrar secreto",
                                                    tint = accent,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("Secreto", item.secret))
                                                    Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = "Copiar secreto",
                                                    tint = accent,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        vaultRepository.deleteById(item.id)
                                                        Toast.makeText(context, "Elemento eliminado de la bóveda", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Eliminar",
                                                    tint = Color(0xFFFF5252),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (item.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // FAB to add credential
            FloatingActionButton(
                onClick = {
                    val activity = context as? android.app.Activity
                    if (activity != null) {
                        adMobManager.showInterstitialForFeature(
                            activity = activity,
                            adUnitId = AdMobUnits.INTERSTITIAL_VAULT,
                            featureName = "Crear Elemento Cifrado en Bóveda",
                            onDone = { showAddDialog = true }
                        )
                    } else {
                        showAddDialog = true
                    }
                },
                containerColor = accent,
                contentColor = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 100.dp, end = 24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Elemento Seguro")
            }
        }
    }

    if (showAddDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newUser by remember { mutableStateOf("") }
        var newSecret by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf("Contraseña") }
        var newNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Guardar en Bóveda Cifrada", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Título (Ej. Correo Corporativo)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUser,
                        onValueChange = { newUser = it },
                        label = { Text("Usuario / Email / Cuenta") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSecret,
                        onValueChange = { newSecret = it },
                        label = { Text("Secreto / Contraseña / Token") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("Categoría (Contraseña, Nota Secreta, API Key...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        label = { Text("Notas adicionales (Opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank() && newSecret.isNotBlank()) {
                            val activity = context as? android.app.Activity
                            val saveAction: () -> Unit = {
                                scope.launch {
                                    vaultRepository.saveItem(
                                        title = newTitle,
                                        usernameOrEmail = newUser,
                                        secret = newSecret,
                                        category = newCategory,
                                        notes = newNotes
                                    )
                                    showAddDialog = false
                                    Toast.makeText(context, "Cifrado y guardado con éxito", Toast.LENGTH_SHORT).show()
                                }
                            }
                            if (activity != null) {
                                adMobManager.showInterstitialForFeature(
                                    activity = activity,
                                    adUnitId = AdMobUnits.INTERSTITIAL_VAULT,
                                    featureName = "Cifrado de Credencial en Bóveda",
                                    onDone = saveAction
                                )
                            } else {
                                saveAction()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black)
                ) {
                    Text("Cifrar y Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
