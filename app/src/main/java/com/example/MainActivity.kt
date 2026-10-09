package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import com.example.ads.AdBannerView
import com.example.ads.AdMobManager
import com.example.ads.AdMobUnits
import com.example.ads.CyberFloatingAdWidget
import com.example.ads.CyberGlobalAdPromptHost
import com.example.data.repository.FirestoreRepository
import com.example.data.vault.VaultRepository
import com.example.security.CyberAppPermissions
import com.example.security.CyberPermissionHubDialog
import com.example.security.CyberSecurityManager
import com.example.telemetry.SignalBoosterManager
import com.example.telemetry.SystemTelemetryManager
import com.example.ui.components.ScientificBackground
import com.example.ui.screens.ai.AIScreen
import com.example.ui.screens.boot.CyberBootScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.optimizer.OptimizerScreen
import com.example.ui.screens.productivity.ProductivityScreen
import com.example.ui.screens.security.SecurityScreen
import com.example.ui.screens.sync.CloudSyncScreen
import com.example.ui.screens.vault.VaultScreen
import com.example.ui.theme.AccentTheme
import com.example.ui.theme.AndroSuiteTheme
import com.example.ui.theme.BorderDim
import com.example.ui.theme.CustomDarkTheme
import com.example.ui.theme.LocalAccentColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import com.example.i18n.AppLanguage
import com.example.i18n.CyberStrings
import com.example.i18n.LanguageManager
import com.example.i18n.LanguageSelectorDialog
import com.example.i18n.LocalAppLanguage

class MainActivity : ComponentActivity() {

    private lateinit var telemetryManager: SystemTelemetryManager
    private lateinit var signalBooster: SignalBoosterManager
    private lateinit var securityManager: CyberSecurityManager
    private lateinit var repository: FirestoreRepository
    private lateinit var vaultRepository: VaultRepository
    private lateinit var adMobManager: AdMobManager
    private lateinit var languageManager: LanguageManager
    private lateinit var junkCleanerManager: com.example.telemetry.JunkCleanerManager

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        telemetryManager = SystemTelemetryManager(applicationContext)
        signalBooster = SignalBoosterManager(applicationContext)
        securityManager = CyberSecurityManager(applicationContext)
        repository = FirestoreRepository.create(applicationContext)
        vaultRepository = VaultRepository.create(applicationContext)
        adMobManager = AdMobManager.getInstance(applicationContext)
        languageManager = LanguageManager.getInstance(applicationContext)
        junkCleanerManager = com.example.telemetry.JunkCleanerManager(applicationContext)

        setContent {
            val currentLanguage by languageManager.currentLanguage.collectAsState()
            var selectedTheme by remember { mutableStateOf(CustomDarkTheme.CYBERPUNK_NEON) }
            var selectedAccent by remember { mutableStateOf(AccentTheme.CYAN) }
            var currentNavIndex by remember { mutableIntStateOf(0) }
            var hasBooted by remember { mutableStateOf(false) }
            var showPermissionDialog by remember { mutableStateOf(false) }
            var showLanguageDialog by remember { mutableStateOf(false) }

            // Accompanist Permissions Manager for Total Device Control
            val permissionsState = rememberMultiplePermissionsState(
                permissions = CyberAppPermissions.REQUIRED_PERMISSIONS
            )

            // Auto-request permissions upon system boot completion
            LaunchedEffect(hasBooted) {
                if (hasBooted && !permissionsState.allPermissionsGranted) {
                    permissionsState.launchMultiplePermissionRequest()
                }
            }

            AndroSuiteTheme(
                selectedTheme = selectedTheme,
                selectedAccent = selectedAccent
            ) {
                CompositionLocalProvider(LocalAppLanguage provides currentLanguage) {
                    if (!hasBooted) {
                        CyberBootScreen(onBootComplete = { hasBooted = true })
                    } else {
                        // BackHandler returns to Dashboard if on a secondary screen
                        BackHandler(enabled = currentNavIndex != 0) {
                            currentNavIndex = 0
                        }

                        // Cyber Permissions Hub Modal
                        if (showPermissionDialog) {
                            CyberPermissionHubDialog(
                                permissionsState = permissionsState,
                                onDismiss = { showPermissionDialog = false }
                            )
                        }

                        // Multi-Language Selector Modal
                        if (showLanguageDialog) {
                            LanguageSelectorDialog(
                                currentLanguage = currentLanguage,
                                onSelectLanguage = {
                                    languageManager.setLanguage(it)
                                    showLanguageDialog = false
                                },
                                onDismiss = { showLanguageDialog = false }
                            )
                        }

                        // Global Sponsored Ad Prompt & Interstitial Host
                        CyberGlobalAdPromptHost(adMobManager = adMobManager)

                        ScientificBackground {
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                containerColor = Color.Transparent,
                                topBar = {
                                    CyberTopBar(
                                        currentNavIndex = currentNavIndex,
                                        currentLanguage = currentLanguage,
                                        allPermissionsGranted = permissionsState.allPermissionsGranted,
                                        onOpenPermissions = { showPermissionDialog = true },
                                        onThemeToggle = {
                                            selectedTheme = when (selectedTheme) {
                                                CustomDarkTheme.CYBERPUNK_NEON -> CustomDarkTheme.OLED_BLACK
                                                CustomDarkTheme.OLED_BLACK -> CustomDarkTheme.MATRIX_HACKER
                                                CustomDarkTheme.MATRIX_HACKER -> CustomDarkTheme.DEEP_SLATE
                                                CustomDarkTheme.DEEP_SLATE -> CustomDarkTheme.SYSTEM
                                                CustomDarkTheme.SYSTEM -> CustomDarkTheme.CYBERPUNK_NEON
                                            }
                                        },
                                        onOpenLanguage = { showLanguageDialog = true }
                                    )
                                },
                                bottomBar = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .navigationBarsPadding()
                                    ) {
                                        // Google AdMob Real Banner VIP (ca-app-pub-1495262574338316/2452733430)
                                        AdBannerView(
                                            adUnitId = AdMobUnits.BANNER_VIP,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        )

                                        CyberBottomNav(
                                            currentIndex = currentNavIndex,
                                            currentLanguage = currentLanguage,
                                        onSelectIndex = { targetIndex ->
                                            if (targetIndex != currentNavIndex) {
                                                val adUnit = when (targetIndex) {
                                                    2 -> AdMobUnits.INTERSTITIAL_OPTIMIZER
                                                    3 -> AdMobUnits.INTERSTITIAL_SECURITY
                                                    4 -> AdMobUnits.INTERSTITIAL_VAULT
                                                    5 -> AdMobUnits.INTERSTITIAL_AI
                                                    else -> null
                                                }
                                                if (adUnit != null) {
                                                    adMobManager.showInterstitialForFeature(
                                                        activity = this@MainActivity,
                                                        adUnitId = adUnit,
                                                        featureName = when (targetIndex) {
                                                            2 -> "Potenciador & Wi-Fi"
                                                            3 -> "Escudo Blindado"
                                                            4 -> "Bóveda Cifrada"
                                                            5 -> "Inteligencia Artificial"
                                                            else -> "Herramienta"
                                                        }
                                                    ) {
                                                        currentNavIndex = targetIndex
                                                    }
                                                } else {
                                                    currentNavIndex = targetIndex
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                Crossfade(targetState = currentNavIndex, label = "screen_crossfade") { index ->
                                    when (index) {
                                        0 -> DashboardScreen(
                                            telemetryManager = telemetryManager,
                                            signalBooster = signalBooster,
                                            securityManager = securityManager,
                                            adMobManager = adMobManager,
                                            permissionsState = permissionsState,
                                            onOpenPermissions = { showPermissionDialog = true },
                                            onNavigateToProductivity = { currentNavIndex = 1 },
                                            onNavigateToOptimizer = { currentNavIndex = 2 },
                                            onNavigateToSecurity = { currentNavIndex = 3 },
                                            onNavigateToVault = { currentNavIndex = 4 },
                                            onNavigateToAI = { currentNavIndex = 5 },
                                            onNavigateToSync = { currentNavIndex = 6 }
                                        )
                                        1 -> ProductivityScreen(
                                            repository = repository,
                                            adMobManager = adMobManager
                                        )
                                        2 -> OptimizerScreen(
                                            telemetryManager = telemetryManager,
                                            signalBooster = signalBooster,
                                            adMobManager = adMobManager,
                                            junkCleanerManager = junkCleanerManager
                                        )
                                        3 -> SecurityScreen(
                                            securityManager = securityManager,
                                            adMobManager = adMobManager,
                                            permissionsState = permissionsState,
                                            onOpenPermissions = { showPermissionDialog = true },
                                            onNavigateToVault = { currentNavIndex = 4 }
                                        )
                                        4 -> VaultScreen(
                                            vaultRepository = vaultRepository,
                                            adMobManager = adMobManager
                                        )
                                        5 -> AIScreen(
                                            telemetryManager = telemetryManager,
                                            adMobManager = adMobManager
                                        )
                                        6 -> CloudSyncScreen(
                                            repository = repository,
                                            currentTheme = selectedTheme,
                                            onThemeChange = { selectedTheme = it },
                                            currentAccent = selectedAccent,
                                            onAccentChange = { selectedAccent = it },
                                            adMobManager = adMobManager
                                        )
                                    }
                                }

                                // Floating Interactive Cyber Ad Widget (Prestamevip + Taxigord + Counter)
                                CyberFloatingAdWidget(
                                    adMobManager = adMobManager,
                                    modifier = Modifier.align(Alignment.BottomEnd)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun CyberTopBar(
    currentNavIndex: Int,
    currentLanguage: AppLanguage,
    allPermissionsGranted: Boolean,
    onOpenPermissions: () -> Unit,
    onThemeToggle: () -> Unit,
    onOpenLanguage: () -> Unit
) {
    val accent = LocalAccentColor.current
    val title = CyberStrings.navTitle(currentNavIndex, currentLanguage)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color(0xCC070B14)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Multi-Language selector button
                IconButton(onClick = onOpenLanguage) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = CyberStrings.selectLanguage(currentLanguage),
                        tint = accent
                    )
                }

                // Control Total permissions button
                IconButton(onClick = onOpenPermissions) {
                    Icon(
                        imageVector = if (allPermissionsGranted) Icons.Default.Shield else Icons.Default.Security,
                        contentDescription = "Control Total de Permisos",
                        tint = if (allPermissionsGranted) Color(0xFF00E676) else Color(0xFFFF9100)
                    )
                }

                IconButton(onClick = onThemeToggle) {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = "Cambiar tema oscuro",
                        tint = accent
                    )
                }
            }
        }
    }
}

data class NavItem(val label: String, val icon: ImageVector)

@Composable
fun CyberBottomNav(
    currentIndex: Int,
    currentLanguage: AppLanguage,
    onSelectIndex: (Int) -> Unit
) {
    val accent = LocalAccentColor.current
    val items = listOf(
        NavItem(CyberStrings.bottomNavLabel(0, currentLanguage), Icons.Default.Dashboard),
        NavItem(CyberStrings.bottomNavLabel(1, currentLanguage), Icons.Default.Timer),
        NavItem(CyberStrings.bottomNavLabel(2, currentLanguage), Icons.Default.Sensors),
        NavItem(CyberStrings.bottomNavLabel(3, currentLanguage), Icons.Default.Shield),
        NavItem(CyberStrings.bottomNavLabel(4, currentLanguage), Icons.Default.Lock),
        NavItem(CyberStrings.bottomNavLabel(5, currentLanguage), Icons.Default.AutoAwesome),
        NavItem(CyberStrings.bottomNavLabel(6, currentLanguage), Icons.Default.CloudSync)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xEE090D18),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(BorderDim, accent.copy(alpha = 0.4f), BorderDim))
        )
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = currentIndex == index
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelectIndex(index) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(19.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = accent,
                        indicatorColor = accent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
