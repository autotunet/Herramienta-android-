package com.example.ui.screens.ai

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobManager
import com.example.ads.AdMobUnits
import com.example.ai.GeminiService
import com.example.telemetry.SystemTelemetryManager
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberStatusPill
import com.example.ui.theme.BorderDim
import com.example.ui.theme.LocalAccentColor
import kotlinx.coroutines.launch

data class ChatMessage(
    val isUser: Boolean,
    val text: String
)

@Composable
fun AIScreen(
    telemetryManager: SystemTelemetryManager,
    adMobManager: AdMobManager
) {
    var selectedToolIndex by remember { mutableIntStateOf(0) }
    val tools = listOf("Asistente IA", "Resumidor", "Desglosador Tareas", "Doctor Batería IA", "Asistente Código")
    val accent = LocalAccentColor.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedToolIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedToolIndex]),
                    color = accent
                )
            }
        ) {
            tools.forEachIndexed { index, name ->
                Tab(
                    selected = selectedToolIndex == index,
                    onClick = { selectedToolIndex = index },
                    text = {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedToolIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedToolIndex == index) accent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedToolIndex) {
                0 -> AIChatTool(adMobManager)
                1 -> AISummarizerTool(adMobManager)
                2 -> AITaskBreakerTool(adMobManager)
                3 -> AIBatteryAdvisorTool(telemetryManager, adMobManager)
                4 -> AICodeAssistantTool(adMobManager)
            }
        }
    }
}

// 1. FREE AI CHAT
@Composable
fun AIChatTool(adMobManager: AdMobManager) {
    val messages = remember {
        mutableStateListOf(
            ChatMessage(false, "¡Hola! Soy AndroAI, tu asistente inteligente cibertecnológico integrado. ¿En qué puedo ayudarte hoy?")
        )
    }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val accent = LocalAccentColor.current
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (msg.isUser) accent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (msg.isUser) accent.copy(alpha = 0.5f) else BorderDim),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (msg.isUser) "TÚ" else "ANDRO AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (msg.isUser) accent else Color(0xFF00E676),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = accent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Procesando con Gemini 2.5 Flash...", style = MaterialTheme.typography.labelSmall, color = accent)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Escribe tu consulta a la IA...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (inputText.isNotBlank() && !isLoading) {
                        val prompt = inputText
                        val activity = context as? android.app.Activity
                        val sendAction: () -> Unit = {
                            messages.add(ChatMessage(true, prompt))
                            inputText = ""
                            isLoading = true
                            scope.launch {
                                val res = GeminiService.generateContent(prompt)
                                isLoading = false
                                res.onSuccess { reply ->
                                    messages.add(ChatMessage(false, reply))
                                }.onFailure { err ->
                                    messages.add(ChatMessage(false, "Error: ${err.message}"))
                                }
                            }
                        }

                        if (activity != null) {
                            adMobManager.showInterstitialForFeature(
                                activity = activity,
                                adUnitId = AdMobUnits.INTERSTITIAL_AI,
                                featureName = "IA Gemini 2.5 Flash",
                                onDone = sendAction
                            )
                        } else {
                            sendAction()
                        }
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black),
                modifier = Modifier.size(50.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar", modifier = Modifier.size(20.dp))
            }
        }
    }
}

// 2. AI SUMMARIZER
@Composable
fun AISummarizerTool(adMobManager: AdMobManager) {
    val context = LocalContext.current
    var sourceText by remember { mutableStateOf("") }
    var summaryResult by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val accent = LocalAccentColor.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            CyberCard {
                Column {
                    Text(text = "RESUMIDOR INTELIGENTE DE TEXTO", style = MaterialTheme.typography.titleMedium, color = accent)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pega cualquier artículo, reunión o apunte extenso. La IA extraerá los puntos clave en segundos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = sourceText,
                        onValueChange = { sourceText = it },
                        placeholder = { Text("Pega tu texto aquí...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (sourceText.isNotBlank()) {
                                val activity = context as? Activity
                                val action: () -> Unit = {
                                    isProcessing = true
                                    scope.launch {
                                        val prompt = "Resume el siguiente texto en puntos clave claros, concisos y accionables:\n\n$sourceText"
                                        val res = GeminiService.generateContent(prompt)
                                        isProcessing = false
                                        res.onSuccess { summaryResult = it }
                                            .onFailure { summaryResult = "Error al resumir: ${it.message}" }
                                    }
                                }
                                if (activity != null) {
                                    adMobManager.showInterstitialForFeature(
                                        activity = activity,
                                        adUnitId = AdMobUnits.INTERSTITIAL_AI,
                                        featureName = "Resumidor de Texto con IA",
                                        onDone = action
                                    )
                                } else {
                                    action()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isProcessing && sourceText.isNotBlank()
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Resumiendo...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generar Resumen")
                        }
                    }
                }
            }
        }

        if (summaryResult != null) {
            item {
                CyberCard(glowColor = Color(0xFF00E676)) {
                    Column {
                        CyberStatusPill(text = "RESUMEN EJECUTIVO", isActive = true, activeColor = Color(0xFF00E676))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = summaryResult ?: "", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

// 3. AI TASK BREAKER
@Composable
fun AITaskBreakerTool(adMobManager: AdMobManager) {
    val context = LocalContext.current
    var goalText by remember { mutableStateOf("") }
    var brokenTasks by remember { mutableStateOf<String?>(null) }
    var isBreaking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val accent = LocalAccentColor.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            CyberCard {
                Column {
                    Text(text = "DESGLOSADOR DE METAS EN TAREAS", style = MaterialTheme.typography.titleMedium, color = accent)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Escribe cualquier meta o proyecto grande (ej: 'Lanzar mi tienda online', 'Organizar un viaje a Japón') y la IA lo dividirá en tareas paso a paso.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = goalText,
                        onValueChange = { goalText = it },
                        placeholder = { Text("Escribe tu meta o proyecto...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (goalText.isNotBlank()) {
                                val activity = context as? Activity
                                val action: () -> Unit = {
                                    isBreaking = true
                                    scope.launch {
                                        val prompt = "Desglosa la siguiente meta en un plan secuencial de 5 a 7 tareas concretas, ordenadas por prioridad (ALTA, MEDIA, BAJA) con una breve explicación para cada una:\n\nMeta: $goalText"
                                        val res = GeminiService.generateContent(prompt)
                                        isBreaking = false
                                        res.onSuccess { brokenTasks = it }
                                            .onFailure { brokenTasks = "Error: ${it.message}" }
                                    }
                                }
                                if (activity != null) {
                                    adMobManager.showInterstitialForFeature(
                                        activity = activity,
                                        adUnitId = AdMobUnits.INTERSTITIAL_AI,
                                        featureName = "Desglosador de Metas con IA",
                                        onDone = action
                                    )
                                } else {
                                    action()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isBreaking && goalText.isNotBlank()
                    ) {
                        if (isBreaking) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Desglosando...")
                        } else {
                            Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generar Plan de Tareas")
                        }
                    }
                }
            }
        }

        if (brokenTasks != null) {
            item {
                CyberCard(glowColor = Color(0xFFFFAB00)) {
                    Column {
                        CyberStatusPill(text = "PLAN ACCIONABLE GENERADO", isActive = true, activeColor = Color(0xFFFFAB00))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = brokenTasks ?: "", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

// 4. AI BATTERY & HARDWARE ADVISOR
@Composable
fun AIBatteryAdvisorTool(telemetryManager: SystemTelemetryManager, adMobManager: AdMobManager) {
    val context = LocalContext.current
    val battery by telemetryManager.batteryState.collectAsState()
    val memory by telemetryManager.memoryState.collectAsState()
    val storage by telemetryManager.storageState.collectAsState()
    var aiAdvice by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val accent = LocalAccentColor.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            CyberCard {
                Column {
                    Text(text = "DOCTOR DE RENDIMIENTO & BATERÍA IA", style = MaterialTheme.typography.titleMedium, color = accent)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "La IA evaluará los valores reales de tu dispositivo (Batería a ${battery.levelPercent}%, Temp: ${battery.temperatureCelsius}°C, RAM: ${memory.usedPercent}% en uso, Almacenamiento: ${storage.usedPercent}% ocupado) y generará recomendaciones técnicas exactas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val activity = context as? Activity
                            val action: () -> Unit = {
                                isAnalyzing = true
                                scope.launch {
                                    val prompt = "Actúa como un ingeniero experto en optimización de sistemas Android. Analiza estos datos reales del dispositivo:\n" +
                                            "- Nivel de batería: ${battery.levelPercent}%\n" +
                                            "- Temperatura de batería: ${battery.temperatureCelsius}°C\n" +
                                            "- Estado de carga: ${battery.chargeType}\n" +
                                            "- Salud de batería: ${battery.healthStatus}\n" +
                                            "- RAM en uso: ${memory.usedPercent}% (${memory.usedRamMb}MB de ${memory.totalRamMb}MB)\n" +
                                            "- Espacio usado: ${storage.usedPercent}% (${storage.usedGb}GB de ${storage.totalGb}GB)\n\n" +
                                            "Genera un diagnóstico técnico y 4 recomendaciones prioritarias en español para alargar la vida útil de la batería y mantener la máxima fluidez."
                                    val res = GeminiService.generateContent(prompt)
                                    isAnalyzing = false
                                    res.onSuccess { aiAdvice = it }
                                        .onFailure { aiAdvice = "Error en análisis: ${it.message}" }
                                }
                            }
                            if (activity != null) {
                                adMobManager.showInterstitialForFeature(
                                    activity = activity,
                                    adUnitId = AdMobUnits.INTERSTITIAL_AI,
                                    featureName = "Doctor de Batería con IA",
                                    onDone = action
                                )
                            } else {
                                action()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isAnalyzing
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analizando Hardware...")
                        } else {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Diagnosticar con IA")
                        }
                    }
                }
            }
        }

        if (aiAdvice != null) {
            item {
                CyberCard(glowColor = Color(0xFF00E676)) {
                    Column {
                        CyberStatusPill(text = "DIAGNÓSTICO TÉCNICO IA", isActive = true, activeColor = Color(0xFF00E676))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = aiAdvice ?: "", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

// 5. AI CODE ASSISTANT
@Composable
fun AICodeAssistantTool(adMobManager: AdMobManager) {
    val context = LocalContext.current
    var codeQuery by remember { mutableStateOf("") }
    var codeResult by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val accent = LocalAccentColor.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            CyberCard {
                Column {
                    Text(text = "ASISTENTE DE CÓDIGO & SCRIPTS", style = MaterialTheme.typography.titleMedium, color = accent)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pide snippets de código Kotlin, Bash, Python, SQL o resuelve dudas de programación directamente desde tu teléfono.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = codeQuery,
                        onValueChange = { codeQuery = it },
                        placeholder = { Text("Ej: ¿Cómo crear un Flow reactivo en Kotlin?") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (codeQuery.isNotBlank()) {
                                val activity = context as? Activity
                                val action: () -> Unit = {
                                    isGenerating = true
                                    scope.launch {
                                        val prompt = "Eres un senior software engineer. Responde a la siguiente consulta con código limpio y una explicación concisa:\n\n$codeQuery"
                                        val res = GeminiService.generateContent(prompt)
                                        isGenerating = false
                                        res.onSuccess { codeResult = it }
                                            .onFailure { codeResult = "Error: ${it.message}" }
                                    }
                                }
                                if (activity != null) {
                                    adMobManager.showInterstitialForFeature(
                                        activity = activity,
                                        adUnitId = AdMobUnits.INTERSTITIAL_AI,
                                        featureName = "Asistente de Código con IA",
                                        onDone = action
                                    )
                                } else {
                                    action()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isGenerating && codeQuery.isNotBlank()
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generando...")
                        } else {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Consultar Código")
                        }
                    }
                }
            }
        }

        if (codeResult != null) {
            item {
                CyberCard(glowColor = Color(0xFFD500F9)) {
                    Column {
                        CyberStatusPill(text = "RESPUESTA DE INGENIERÍA", isActive = true, activeColor = Color(0xFFD500F9))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = codeResult ?: "", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
