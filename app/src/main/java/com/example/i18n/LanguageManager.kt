package com.example.i18n

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val flag: String,
    val region: String
) {
    ES("es", "Español", "🇪🇸", "España / Latinoamérica"),
    EN("en", "English", "🇺🇸", "United States / Global"),
    PT("pt", "Português", "🇧🇷", "Brasil / Portugal"),
    FR("fr", "Français", "🇫🇷", "France / Canada"),
    DE("de", "Deutsch", "🇩🇪", "Deutschland / Österreich"),
    IT("it", "Italiano", "🇮🇹", "Italia / Svizzera")
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.ES }

class LanguageManager private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("andro_language_prefs", Context.MODE_PRIVATE)

    private val _currentLanguage = MutableStateFlow(loadInitialLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private fun loadInitialLanguage(): AppLanguage {
        val savedCode = prefs.getString("selected_language_code", null)
        if (savedCode != null) {
            return AppLanguage.entries.find { it.code == savedCode } ?: AppLanguage.ES
        }
        val defaultLocale = java.util.Locale.getDefault().language
        return AppLanguage.entries.find { it.code.equals(defaultLocale, ignoreCase = true) } ?: AppLanguage.ES
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("selected_language_code", language.code).apply()
    }

    companion object {
        @Volatile
        private var INSTANCE: LanguageManager? = null

        fun getInstance(context: Context): LanguageManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LanguageManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

/**
 * High-performance localized dictionary for all application strings
 */
object CyberStrings {

    fun navTitle(index: Int, lang: AppLanguage): String {
        return when (index) {
            0 -> when (lang) {
                AppLanguage.ES -> "PANEL DE CONTROL"
                AppLanguage.EN -> "COMMAND CENTER"
                AppLanguage.PT -> "PAINEL DE CONTROLE"
                AppLanguage.FR -> "TABLEAU DE BORD"
                AppLanguage.DE -> "KONTROLLZENTRUM"
                AppLanguage.IT -> "PANNELLO DI CONTROLLO"
            }
            1 -> when (lang) {
                AppLanguage.ES -> "PRODUCTIVIDAD"
                AppLanguage.EN -> "PRODUCTIVITY"
                AppLanguage.PT -> "PRODUTIVIDADE"
                AppLanguage.FR -> "PRODUCTIVITÉ"
                AppLanguage.DE -> "PRODUKTIVITÄT"
                AppLanguage.IT -> "PRODUTTIVITÀ"
            }
            2 -> when (lang) {
                AppLanguage.ES -> "POTENCIA & WI-FI"
                AppLanguage.EN -> "POWER & WI-FI"
                AppLanguage.PT -> "POTÊNCIA & WI-FI"
                AppLanguage.FR -> "PUISSANCE & WI-FI"
                AppLanguage.DE -> "LEISTUNG & WI-FI"
                AppLanguage.IT -> "POTENZA & WI-FI"
            }
            3 -> when (lang) {
                AppLanguage.ES -> "ESCUDO BLINDADO"
                AppLanguage.EN -> "ARMORED SHIELD"
                AppLanguage.PT -> "ESCUDO BLINDADO"
                AppLanguage.FR -> "BOUCLIER BLINDÉ"
                AppLanguage.DE -> "SCHUTZSCHILD"
                AppLanguage.IT -> "SCUDO BLINDATO"
            }
            4 -> when (lang) {
                AppLanguage.ES -> "BÓVEDA CIFRADA"
                AppLanguage.EN -> "ENCRYPTED VAULT"
                AppLanguage.PT -> "COFRE CRIPTOGRAFADO"
                AppLanguage.FR -> "COFFRE CRYPTÉ"
                AppLanguage.DE -> "VERSCHLÜSSELTER TRESOR"
                AppLanguage.IT -> "CAVEAU CRITTOGRAFATO"
            }
            5 -> when (lang) {
                AppLanguage.ES -> "INTELIGENCIA ARTIFICIAL"
                AppLanguage.EN -> "ARTIFICIAL INTELLIGENCE"
                AppLanguage.PT -> "INTELIGÊNCIA ARTIFICIAL"
                AppLanguage.FR -> "INTELLIGENCE ARTIFICIELLE"
                AppLanguage.DE -> "KÜNSTLICHE INTELLIGENZ"
                AppLanguage.IT -> "INTELLIGENZA ARTIFICIALE"
            }
            6 -> when (lang) {
                AppLanguage.ES -> "NUBE MULTI-DISPOSITIVO"
                AppLanguage.EN -> "MULTI-DEVICE CLOUD"
                AppLanguage.PT -> "NUVEM MULTIDISPOSITIVO"
                AppLanguage.FR -> "CLOUD MULTI-APPAREILS"
                AppLanguage.DE -> "MULTI-GERÄTE-CLOUD"
                AppLanguage.IT -> "CLOUD MULTI-DISPOSITIVO"
            }
            else -> "ANDROSUITE PRO"
        }
    }

    fun speedTestTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "TEST DE VELOCIDAD DE RED"
        AppLanguage.EN -> "NETWORK SPEED TEST"
        AppLanguage.PT -> "TESTE DE VELOCIDADE DE REDE"
        AppLanguage.FR -> "TEST DE VITESSE RÉSEAU"
        AppLanguage.DE -> "NETZWERK-GESCHWINDIGKEITSTEST"
        AppLanguage.IT -> "TEST DI VELOCITÀ DI RETE"
    }

    fun download(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "DESCARGA"
        AppLanguage.EN -> "DOWNLOAD"
        AppLanguage.PT -> "DOWNLOAD"
        AppLanguage.FR -> "TÉLÉCHARGEMENT"
        AppLanguage.DE -> "DOWNLOAD"
        AppLanguage.IT -> "DOWNLOAD"
    }

    fun upload(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "SUBIDA"
        AppLanguage.EN -> "UPLOAD"
        AppLanguage.PT -> "UPLOAD"
        AppLanguage.FR -> "TÉLÉVERSEMENT"
        AppLanguage.DE -> "UPLOAD"
        AppLanguage.IT -> "CARICAMENTO"
    }

    fun startSpeedTest(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "INICIAR TEST DE VELOCIDAD"
        AppLanguage.EN -> "START SPEED TEST"
        AppLanguage.PT -> "INICIAR TESTE DE VELOCIDADE"
        AppLanguage.FR -> "DÉMARRER LE TEST DE VITESSE"
        AppLanguage.DE -> "GESCHWINDIGKEITSTEST STARTEN"
        AppLanguage.IT -> "AVVIA TEST DI VELOCITÀ"
    }

    fun selectLanguage(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "SELECCIONAR IDIOMA"
        AppLanguage.EN -> "SELECT LANGUAGE"
        AppLanguage.PT -> "SELECIONAR IDIOMA"
        AppLanguage.FR -> "CHOISIR LA LANGUE"
        AppLanguage.DE -> "SPRACHE AUSWÄHLEN"
        AppLanguage.IT -> "SELEZIONA LINGUA"
    }

    fun languageSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "Cambia instantáneamente el idioma de toda la interfaz"
        AppLanguage.EN -> "Instantly switch the entire interface language"
        AppLanguage.PT -> "Altere instantaneamente o idioma de toda a interface"
        AppLanguage.FR -> "Changez instantanément la langue de toute l'interface"
        AppLanguage.DE -> "Ändern Sie sofort die Sprache der Benutzeroberfläche"
        AppLanguage.IT -> "Cambia istantaneamente la lingua dell'intera interfaccia"
    }

    fun permissionsHub(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "Centro de Permisos del Teléfono"
        AppLanguage.EN -> "Device Permissions Hub"
        AppLanguage.PT -> "Central de Permissões do Dispositivo"
        AppLanguage.FR -> "Centre des Autorisations de l'Appareil"
        AppLanguage.DE -> "Geräteberechtigungs-Zentrum"
        AppLanguage.IT -> "Centro Autorizzazioni del Dispositivo"
    }

    fun ramCleaner(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "Limpiador Profundo de RAM"
        AppLanguage.EN -> "Deep RAM Cleaner"
        AppLanguage.PT -> "Limpador Profundo de RAM"
        AppLanguage.FR -> "Nettoyeur Approfondi de RAM"
        AppLanguage.DE -> "Tiefer RAM-Reiniger"
        AppLanguage.IT -> "Pulitore Profondo di RAM"
    }

    fun wifiBooster(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "Potenciador Wi-Fi & Señal"
        AppLanguage.EN -> "Wi-Fi & Signal Booster"
        AppLanguage.PT -> "Amplificador de Wi-Fi e Sinal"
        AppLanguage.FR -> "Booster Wi-Fi & Signal"
        AppLanguage.DE -> "WLAN- & Signalverstärker"
        AppLanguage.IT -> "Potenziatore Wi-Fi e Segnale"
    }

    fun spyDetector(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "Detector de Cámaras y Espías"
        AppLanguage.EN -> "Hidden Camera & Spy Detector"
        AppLanguage.PT -> "Detector de Câmeras e Espiões"
        AppLanguage.FR -> "Détecteur de Caméras et Espions"
        AppLanguage.DE -> "Spionagekamera-Detektor"
        AppLanguage.IT -> "Rilevatore di Telecamere e Spie"
    }

    fun close(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "CERRAR"
        AppLanguage.EN -> "CLOSE"
        AppLanguage.PT -> "FECHAR"
        AppLanguage.FR -> "FERMER"
        AppLanguage.DE -> "SCHLIESSEN"
        AppLanguage.IT -> "CHIUDI"
    }

    fun bottomNavLabel(index: Int, lang: AppLanguage): String = when (index) {
        0 -> when (lang) {
            AppLanguage.ES -> "Inicio"
            AppLanguage.EN -> "Home"
            AppLanguage.PT -> "Início"
            AppLanguage.FR -> "Accueil"
            AppLanguage.DE -> "Start"
            AppLanguage.IT -> "Home"
        }
        1 -> when (lang) {
            AppLanguage.ES -> "Tareas"
            AppLanguage.EN -> "Tasks"
            AppLanguage.PT -> "Tarefas"
            AppLanguage.FR -> "Tâches"
            AppLanguage.DE -> "Aufgaben"
            AppLanguage.IT -> "Attività"
        }
        2 -> when (lang) {
            AppLanguage.ES -> "Potencia"
            AppLanguage.EN -> "Power"
            AppLanguage.PT -> "Potência"
            AppLanguage.FR -> "Puissance"
            AppLanguage.DE -> "Leistung"
            AppLanguage.IT -> "Potenza"
        }
        3 -> when (lang) {
            AppLanguage.ES -> "Blindaje"
            AppLanguage.EN -> "Shield"
            AppLanguage.PT -> "Blindagem"
            AppLanguage.FR -> "Bouclier"
            AppLanguage.DE -> "Schild"
            AppLanguage.IT -> "Scudo"
        }
        4 -> when (lang) {
            AppLanguage.ES -> "Bóveda"
            AppLanguage.EN -> "Vault"
            AppLanguage.PT -> "Cofre"
            AppLanguage.FR -> "Coffre"
            AppLanguage.DE -> "Tresor"
            AppLanguage.IT -> "Caveau"
        }
        5 -> when (lang) {
            AppLanguage.ES -> "IA"
            AppLanguage.EN -> "AI"
            AppLanguage.PT -> "IA"
            AppLanguage.FR -> "IA"
            AppLanguage.DE -> "KI"
            AppLanguage.IT -> "IA"
        }
        6 -> when (lang) {
            AppLanguage.ES -> "Nube"
            AppLanguage.EN -> "Cloud"
            AppLanguage.PT -> "Nuvem"
            AppLanguage.FR -> "Cloud"
            AppLanguage.DE -> "Cloud"
            AppLanguage.IT -> "Cloud"
        }
        else -> "Menu"
    }

    fun junkCleanerTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "LIMPIADOR DE ARCHIVOS BASURA"
        AppLanguage.EN -> "JUNK FILE CLEANER"
        AppLanguage.PT -> "LIMPADOR DE ARQUIVOS LIXO"
        AppLanguage.FR -> "NETTOYEUR DE FICHIERS INDÉSIRABLES"
        AppLanguage.DE -> "JUNK-DATEIEN BEREINIGUNG"
        AppLanguage.IT -> "PULITORE FILE SPAZZATURA"
    }

    fun scanStorage(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "Analizar Almacenamiento"
        AppLanguage.EN -> "Scan Storage"
        AppLanguage.PT -> "Escanear Armazenamento"
        AppLanguage.FR -> "Analyser le Stockage"
        AppLanguage.DE -> "Speicher Scannen"
        AppLanguage.IT -> "Scansiona Memoria"
    }

    fun cleanSelected(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "Liberar Espacio Seleccionado"
        AppLanguage.EN -> "Clean Selected Files"
        AppLanguage.PT -> "Liberar Espaço Selecionado"
        AppLanguage.FR -> "Libérer l'Espace Sélectionné"
        AppLanguage.DE -> "Ausgewählte Bereinigen"
        AppLanguage.IT -> "Libera Spazio Selezionato"
    }

    fun cacheFiles(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "Caché de Aplicaciones"
        AppLanguage.EN -> "Application Cache"
        AppLanguage.PT -> "Cache de Aplicativos"
        AppLanguage.FR -> "Cache d'Applications"
        AppLanguage.DE -> "Anwendungs-Cache"
        AppLanguage.IT -> "Cache Applicazioni"
    }

    fun tempFiles(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "Archivos Temporales"
        AppLanguage.EN -> "Temporary Files"
        AppLanguage.PT -> "Arquivos Temporários"
        AppLanguage.FR -> "Fichiers Temporaires"
        AppLanguage.DE -> "Temporäre Dateien"
        AppLanguage.IT -> "File Temporanei"
    }

    fun duplicateImages(lang: AppLanguage): String = when (lang) {
        AppLanguage.ES -> "Imágenes Duplicadas"
        AppLanguage.EN -> "Duplicate Images"
        AppLanguage.PT -> "Imagens Duplicadas"
        AppLanguage.FR -> "Images Dupliquées"
        AppLanguage.DE -> "Doppelte Bilder"
        AppLanguage.IT -> "Immagini Duplicate"
    }
}
