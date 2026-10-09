package com.example.ui.screens.productivity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.CountDownTimer
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.model.ClipboardItem
import com.example.data.model.HabitItem
import com.example.data.model.NoteItem
import com.example.data.model.TaskItem
import com.example.data.repository.FirestoreRepository
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberStatusPill
import com.example.ui.theme.BorderDim
import com.example.ui.theme.LocalAccentColor
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.ads.AdMobManager
import com.example.ads.AdMobUnits
import android.app.Activity
import androidx.compose.ui.platform.LocalContext

@Composable
fun ProductivityScreen(
    repository: FirestoreRepository,
    adMobManager: AdMobManager
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Notas", "Tareas", "Pomodoro", "Hábitos", "Conversor", "Portapapeles")
    val accent = LocalAccentColor.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = accent
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        if (selectedTab != index) {
                            val activity = context as? Activity
                            if (activity != null) {
                                adMobManager.showInterstitialForFeature(
                                    activity = activity,
                                    adUnitId = AdMobUnits.INTERSTITIAL_TAXIGORD_V1,
                                    featureName = "Herramienta $title",
                                    onDone = { selectedTab = index }
                                )
                            } else {
                                selectedTab = index
                            }
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) accent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> NotesTab(repository, adMobManager)
                1 -> TasksTab(repository, adMobManager)
                2 -> PomodoroTab(adMobManager)
                3 -> HabitsTab(repository, adMobManager)
                4 -> ConverterTab(adMobManager)
                5 -> ClipboardTab(repository, adMobManager)
            }
        }
    }
}

// --- 1. NOTES TAB ---
@Composable
fun NotesTab(repository: FirestoreRepository, adMobManager: AdMobManager) {
    val context = LocalContext.current
    val notesFlow = remember { repository.observeNotes().catch { emit(emptyList()) } }
    val notes by notesFlow.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val accent = LocalAccentColor.current

    Box(modifier = Modifier.fillMaxSize()) {
        if (notes.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Note,
                title = "No hay notas aún",
                description = "Crea notas con sincronización automática en la nube en tiempo real."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    CyberCard(glowColor = Color(android.graphics.Color.parseColor(note.colorHex.ifBlank { "#00E5FF" }))) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (note.isPinned) {
                                        Icon(Icons.Default.PushPin, contentDescription = "Fijada", tint = accent, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = note.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(onClick = { scope.launch { repository.deleteNote(note.id) } }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFFF5252), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CyberStatusPill(text = note.category, isActive = false)
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = accent,
            contentColor = Color.Black
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nueva Nota")
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Ideas") }
        var isPinned by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nueva Nota Rápida", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Contenido") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Categoría (e.g. Trabajo, Ideas, Estudio)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val activity = context as? Activity
                            val saveAction: () -> Unit = {
                                scope.launch {
                                    repository.saveNote(
                                        NoteItem(
                                            title = title,
                                            content = content,
                                            category = category,
                                            isPinned = isPinned
                                        )
                                    )
                                    showDialog = false
                                }
                            }
                            if (activity != null) {
                                adMobManager.showInterstitialForFeature(
                                    activity = activity,
                                    adUnitId = AdMobUnits.INTERSTITIAL_TAXIGORD_V1,
                                    featureName = "Guardar Nota en la Nube",
                                    onDone = saveAction
                                )
                            } else {
                                saveAction()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black)
                ) {
                    Text("Guardar y Sincronizar")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// --- 2. TASKS TAB ---
@Composable
fun TasksTab(repository: FirestoreRepository, adMobManager: AdMobManager) {
    val context = LocalContext.current
    val tasksFlow = remember { repository.observeTasks().catch { emit(emptyList()) } }
    val tasks by tasksFlow.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val accent = LocalAccentColor.current

    Box(modifier = Modifier.fillMaxSize()) {
        if (tasks.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.FormatListBulleted,
                title = "No hay tareas pendientes",
                description = "Organiza tu día con prioridades y metas accionables."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    val priorityColor = when (task.priority) {
                        "HIGH" -> Color(0xFFFF5252)
                        "MEDIUM" -> Color(0xFFFFAB00)
                        else -> Color(0xFF00E676)
                    }

                    CyberCard(glowColor = priorityColor) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { scope.launch { repository.toggleTask(task) } }) {
                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Completar",
                                    tint = if (task.isCompleted) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                if (task.description.isNotBlank()) {
                                    Text(
                                        text = task.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CyberStatusPill(text = task.priority, isActive = true, activeColor = priorityColor)
                                    if (task.dueDate.isNotBlank()) {
                                        CyberStatusPill(text = task.dueDate, isActive = false)
                                    }
                                }
                            }
                            IconButton(onClick = { scope.launch { repository.deleteTask(task.id) } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0x88FF5252))
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = accent,
            contentColor = Color.Black
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nueva Tarea")
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("HIGH") }
        var dueDate by remember { mutableStateOf("Hoy") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nueva Tarea de Rendimiento", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título de la tarea") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                            Button(
                                onClick = { priority = p },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (priority == p) accent else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (priority == p) Color.Black else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(p, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val activity = context as? Activity
                            val createAction: () -> Unit = {
                                scope.launch {
                                    repository.saveTask(
                                        TaskItem(
                                            title = title,
                                            description = description,
                                            priority = priority,
                                            dueDate = dueDate
                                        )
                                    )
                                    showDialog = false
                                }
                            }
                            if (activity != null) {
                                adMobManager.showInterstitialForFeature(
                                    activity = activity,
                                    adUnitId = AdMobUnits.INTERSTITIAL_TAXIGORD_V1,
                                    featureName = "Crear Tarea",
                                    onDone = createAction
                                )
                            } else {
                                createAction()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black)
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

// --- 3. POMODORO FOCUS TIMER ---
@Composable
fun PomodoroTab(adMobManager: AdMobManager) {
    val context = LocalContext.current
    var isRunning by remember { mutableStateOf(false) }
    var isBreak by remember { mutableStateOf(false) }
    var timeLeftSeconds by remember { mutableLongStateOf(25 * 60L) }
    var completedSessions by remember { mutableIntStateOf(0) }
    val accent = LocalAccentColor.current

    DisposableEffect(isRunning, isBreak) {
        var timer: CountDownTimer? = null
        if (isRunning) {
            timer = object : CountDownTimer(timeLeftSeconds * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeftSeconds = millisUntilFinished / 1000
                }

                override fun onFinish() {
                    isRunning = false
                    try {
                        val vibrator = context.getSystemService(Vibrator::class.java)
                        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300), -1))
                    } catch (ignored: Exception) {}

                    if (!isBreak) {
                        completedSessions++
                        isBreak = true
                        timeLeftSeconds = 5 * 60L
                    } else {
                        isBreak = false
                        timeLeftSeconds = 25 * 60L
                    }
                }
            }.start()
        }
        onDispose {
            timer?.cancel()
        }
    }

    val minutes = timeLeftSeconds / 60
    val seconds = timeLeftSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            CyberCard(glowColor = if (isBreak) Color(0xFF00E676) else accent) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CyberStatusPill(
                        text = if (isBreak) "TIEMPO DE DESCANSO" else "MODO ENFOQUE ACTIVO",
                        isActive = isRunning,
                        activeColor = if (isBreak) Color(0xFF00E676) else accent
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBreak) Color(0xFF00E676) else accent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sesiones de concentración completadas: $completedSessions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                if (!isRunning) {
                                    val activity = context as? Activity
                                    if (activity != null) {
                                        adMobManager.showInterstitialForFeature(
                                            activity = activity,
                                            adUnitId = AdMobUnits.INTERSTITIAL_TAXIGORD_V1,
                                            featureName = "Modo Enfoque Pomodoro",
                                            onDone = { isRunning = true }
                                        )
                                    } else {
                                        isRunning = true
                                    }
                                } else {
                                    isRunning = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) Color(0xFFFF5252) else accent,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isRunning) "Pausar" else "Iniciar")
                        }

                        Button(
                            onClick = {
                                isRunning = false
                                isBreak = false
                                timeLeftSeconds = 25 * 60L
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reiniciar")
                        }
                    }
                }
            }
        }

        item {
            CyberCard {
                Column {
                    Text(text = "TÉCNICA POMODORO CIBERNÉTICA", style = MaterialTheme.typography.titleMedium, color = accent)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Enfócate 25 minutos sin interrupciones ni notificaciones.\n2. Descansa 5 minutos para oxigenar tu cerebro.\n3. Cada 4 ciclos, toma un descanso largo de 15 minutos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// --- 4. HABITS TAB ---
@Composable
fun HabitsTab(repository: FirestoreRepository, adMobManager: AdMobManager) {
    val context = LocalContext.current
    val habitsFlow = remember { repository.observeHabits().catch { emit(emptyList()) } }
    val habits by habitsFlow.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val accent = LocalAccentColor.current
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (habits.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.LocalFireDepartment,
                title = "Sin hábitos registrados",
                description = "Crea metas diarias (e.g. Beber 2L de agua, Leer 20 min, Ejercicio) y mantén tus rachas."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(habits, key = { it.id }) { habit ->
                    val isDoneToday = habit.lastCompletedDate == today
                    CyberCard(glowColor = if (isDoneToday) Color(0xFF00E676) else Color(0xFFFFAB00)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x22FFAB00)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFFAB00))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Racha actual: ${habit.streakCount} días consecutivos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFFAB00)
                                )
                            }
                            Button(
                                onClick = {
                                    if (!isDoneToday) {
                                        val activity = context as? Activity
                                        val action: () -> Unit = {
                                            scope.launch { repository.completeHabitToday(habit, today) }
                                        }
                                        if (activity != null) {
                                            adMobManager.showInterstitialForFeature(
                                                activity = activity,
                                                adUnitId = AdMobUnits.INTERSTITIAL_TAXIGORD_V1,
                                                featureName = "Completar Hábito Diario",
                                                onDone = action
                                            )
                                        } else {
                                            action()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDoneToday) Color(0xFF00E676) else accent,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isDoneToday
                            ) {
                                Text(if (isDoneToday) "¡Hecho!" else "Marcar")
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = accent,
            contentColor = Color.Black
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nuevo Hábito")
        }
    }

    if (showDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nuevo Hábito Diario") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del hábito") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val activity = context as? Activity
                            val action: () -> Unit = {
                                scope.launch {
                                    repository.saveHabit(HabitItem(name = name))
                                    showDialog = false
                                }
                            }
                            if (activity != null) {
                                adMobManager.showInterstitialForFeature(
                                    activity = activity,
                                    adUnitId = AdMobUnits.INTERSTITIAL_TAXIGORD_V1,
                                    featureName = "Crear Hábito",
                                    onDone = action
                                )
                            } else {
                                action()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black)
                ) {
                    Text("Crear")
                }
            },
            dismissButton = { Button(onClick = { showDialog = false }) { Text("Cancelar") } }
        )
    }
}

// --- 5. CONVERTER TAB ---
@Composable
fun ConverterTab(adMobManager: AdMobManager) {
    val context = LocalContext.current
    var inputValue by remember { mutableStateOf("1024") }
    var selectedCategory by remember { mutableStateOf("Bytes") }
    val accent = LocalAccentColor.current

    val inputNum = inputValue.toDoubleOrNull() ?: 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CyberCard {
                Column {
                    Text(text = "CONVERSOR RÁPIDO DE RENDIMIENTO", style = MaterialTheme.typography.titleMedium, color = accent)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Bytes", "Temperatura", "Velocidad").forEach { cat ->
                            Button(
                                onClick = {
                                    val activity = context as? Activity
                                    val changeCategory = { selectedCategory = cat }
                                    if (activity != null) {
                                        adMobManager.showInterstitialForFeature(
                                            activity = activity,
                                            adUnitId = AdMobUnits.INTERSTITIAL_TAXIGORD_V1,
                                            featureName = "Conversor de $cat",
                                            onDone = changeCategory
                                        )
                                    } else {
                                        changeCategory()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedCategory == cat) accent else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedCategory == cat) Color.Black else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(cat, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { Text("Valor a convertir") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            CyberCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "RESULTADOS CALCULADOS", style = MaterialTheme.typography.labelLarge, color = accent)
                    when (selectedCategory) {
                        "Bytes" -> {
                            val kb = inputNum / 1024.0
                            val mb = inputNum / (1024.0 * 1024.0)
                            val gb = inputNum / (1024.0 * 1024.0 * 1024.0)
                            ConversionResultRow("Kilobytes (KB)", String.format("%.2f KB", kb))
                            ConversionResultRow("Megabytes (MB)", String.format("%.4f MB", mb))
                            ConversionResultRow("Gigabytes (GB)", String.format("%.6f GB", gb))
                        }
                        "Temperatura" -> {
                            val fahrenheit = (inputNum * 9 / 5) + 32
                            val kelvin = inputNum + 273.15
                            ConversionResultRow("Fahrenheit (°F)", String.format("%.1f °F", fahrenheit))
                            ConversionResultRow("Kelvin (K)", String.format("%.2f K", kelvin))
                        }
                        "Velocidad" -> {
                            val mph = inputNum * 0.621371
                            val ms = inputNum / 3.6
                            ConversionResultRow("Millas por hora (mph)", String.format("%.2f mph", mph))
                            ConversionResultRow("Metros por segundo (m/s)", String.format("%.2f m/s", ms))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversionResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// --- 6. CLIPBOARD TAB ---
@Composable
fun ClipboardTab(repository: FirestoreRepository, adMobManager: AdMobManager) {
    val clipsFlow = remember { repository.observeClipboard().catch { emit(emptyList()) } }
    val clips by clipsFlow.collectAsState(initial = emptyList())
    var snippetText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val accent = LocalAccentColor.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CyberCard {
            Column {
                Text(text = "PORTAPAPELES SINCRONIZADO EN NUBE", style = MaterialTheme.typography.titleMedium, color = accent)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = snippetText,
                        onValueChange = { snippetText = it },
                        placeholder = { Text("Pega o escribe texto aquí...") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (snippetText.isNotBlank()) {
                                val textToSave = snippetText
                                val activity = context as? Activity
                                val saveAction: () -> Unit = {
                                    scope.launch {
                                        repository.saveClipboard(textToSave)
                                        snippetText = ""
                                    }
                                }
                                if (activity != null) {
                                    adMobManager.showInterstitialForFeature(
                                        activity = activity,
                                        adUnitId = AdMobUnits.INTERSTITIAL_TAXIGORD_V1,
                                        featureName = "Guardar en Portapapeles Criptográfico",
                                        onDone = saveAction
                                    )
                                } else {
                                    saveAction()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }

        if (clips.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.ContentCopy,
                title = "Portapapeles vacío",
                description = "Guarda textos o enlaces para acceder a ellos desde tus otros dispositivos en tiempo real."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(clips, key = { it.id }) { clip ->
                    CyberCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = clip.snippet, style = MaterialTheme.typography.bodyMedium)
                                Text(text = clip.sourceLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clipData = ClipData.newPlainText("AndroSuite Clip", clip.snippet)
                                clipboard.setPrimaryClip(clipData)
                                Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = accent)
                            }
                            IconButton(onClick = { scope.launch { repository.deleteClipboard(clip.id) } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0x88FF5252))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = LocalAccentColor.current, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
