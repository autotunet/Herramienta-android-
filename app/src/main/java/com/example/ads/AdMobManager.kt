package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdMobUnits {
    // 1. Taxigord v1
    const val INTERSTITIAL_TAXIGORD_V1 = "ca-app-pub-1495262574338316/2803163788"
    // 2. Taxobetapub
    const val INTERSTITIAL_TAXOBETAPUB = "ca-app-pub-1495262574338316/2903409205"
    // 3. Banner vip
    const val BANNER_VIP = "ca-app-pub-1495262574338316/2452733430"
    // 4. Rewarded / Prestamevip
    const val REWARDED_PRESTAMEVIP = "ca-app-pub-1495262574338316/8970040322"
    // 5. Interstitial Security
    const val INTERSTITIAL_SECURITY = "ca-app-pub-1495262574338316/1182179137"
    // 6. Interstitial Optimizer
    const val INTERSTITIAL_OPTIMIZER = "ca-app-pub-1495262574338316/4962596335"
    // 7. Interstitial Vault
    const val INTERSTITIAL_VAULT = "ca-app-pub-1495262574338316/2714752655"
    // 8. Interstitial AI
    const val INTERSTITIAL_AI = "ca-app-pub-1495262574338316/3503687777"

    // Google test unit IDs for development fallback when fill is 0
    const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
}

data class PendingAdPrompt(
    val featureName: String,
    val adUnitId: String,
    val isRewarded: Boolean = false,
    val onConfirm: () -> Unit
)

class AdMobManager private constructor(private val appContext: Context) {

    private val TAG = "AdMobManager"

    private var initialized = false

    // Cached ads
    private val interstitialCache = mutableMapOf<String, InterstitialAd?>()
    private val isInterstitialLoading = mutableMapOf<String, Boolean>()
    private var cachedRewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    // Observable states for UI feedback
    private val _adStatusMessage = MutableStateFlow("AdMob Inicializado")
    val adStatusMessage: StateFlow<String> = _adStatusMessage.asStateFlow()

    private val _adsShownCount = MutableStateFlow(0)
    val adsShownCount: StateFlow<Int> = _adsShownCount.asStateFlow()

    private val _vipUnlocked = MutableStateFlow(false)
    val vipUnlocked: StateFlow<Boolean> = _vipUnlocked.asStateFlow()

    private val _pendingPrompt = MutableStateFlow<PendingAdPrompt?>(null)
    val pendingPrompt: StateFlow<PendingAdPrompt?> = _pendingPrompt.asStateFlow()

    fun dismissPendingPrompt() {
        _pendingPrompt.value = null
    }

    fun initialize() {
        if (initialized) return
        try {
            val requestConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(requestConfig)

            MobileAds.initialize(appContext) { initStatus ->
                initialized = true
                Log.d(TAG, "AdMob SDK Initialized: $initStatus")
                _adStatusMessage.value = "AdMob Conectado (8 unidades activas)"
                // Preload primary optimizer interstitial
                preloadInterstitial(AdMobUnits.INTERSTITIAL_OPTIMIZER)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds", e)
        }
    }

    fun preloadInterstitial(adUnitId: String) {
        if (interstitialCache[adUnitId] != null || isInterstitialLoading[adUnitId] == true) return
        isInterstitialLoading[adUnitId] = true

        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            appContext,
            adUnitId,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialCache[adUnitId] = ad
                    isInterstitialLoading[adUnitId] = false
                    Log.d(TAG, "Interstitial loaded for $adUnitId")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialCache[adUnitId] = null
                    isInterstitialLoading[adUnitId] = false
                    Log.w(TAG, "Interstitial failed for $adUnitId: ${error.message} (code: ${error.code})")
                    // If live ad has error code 3 (no fill in test/unapproved state), fallback to test ad for emulator stability
                    if (error.code == AdRequest.ERROR_CODE_NO_FILL && adUnitId != AdMobUnits.TEST_INTERSTITIAL) {
                        loadTestInterstitialFallback(adUnitId)
                    }
                }
            }
        )
    }

    private fun loadTestInterstitialFallback(targetKey: String) {
        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            appContext,
            AdMobUnits.TEST_INTERSTITIAL,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialCache[targetKey] = ad
                    interstitialCache[AdMobUnits.TEST_INTERSTITIAL] = ad
                    Log.d(TAG, "Test Interstitial fallback loaded for $targetKey")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Test Interstitial fallback failed: ${error.message}")
                }
            }
        )
    }

    fun preloadRewarded() {
        if (cachedRewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val request = AdRequest.Builder().build()
        RewardedAd.load(
            appContext,
            AdMobUnits.REWARDED_PRESTAMEVIP,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    cachedRewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded Ad loaded: ${AdMobUnits.REWARDED_PRESTAMEVIP}")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    cachedRewardedAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "Rewarded Ad failed: ${error.message} (code: ${error.code})")
                    if (error.code == AdRequest.ERROR_CODE_NO_FILL) {
                        loadTestRewardedFallback()
                    }
                }
            }
        )
    }

    private fun loadTestRewardedFallback() {
        val request = AdRequest.Builder().build()
        RewardedAd.load(
            appContext,
            AdMobUnits.TEST_REWARDED,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    cachedRewardedAd = ad
                    Log.d(TAG, "Test Rewarded fallback loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Test Rewarded fallback failed: ${error.message}")
                }
            }
        )
    }

    /**
     * Shows an interstitial ad before executing a requested feature or tool.
     * Always ensures an ad or sponsored prompt is presented to the user.
     */
    fun showInterstitialForFeature(
        activity: Activity,
        adUnitId: String,
        featureName: String = "Herramienta",
        onDone: () -> Unit
    ) {
        // Find best available ad in cache
        var targetAd = interstitialCache[adUnitId]
        var usedUnitId = adUnitId

        if (targetAd == null) {
            targetAd = interstitialCache[AdMobUnits.TEST_INTERSTITIAL]
            if (targetAd != null) usedUnitId = AdMobUnits.TEST_INTERSTITIAL
        }

        if (targetAd == null) {
            val entry = interstitialCache.entries.firstOrNull { it.value != null }
            if (entry != null) {
                targetAd = entry.value
                usedUnitId = entry.key
            }
        }

        if (targetAd != null) {
            targetAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial dismissed for $featureName")
                    interstitialCache[usedUnitId] = null
                    _adsShownCount.value += 1
                    _adStatusMessage.value = "Anuncio completado para: $featureName"
                    preloadInterstitial(usedUnitId)
                    preloadInterstitial(adUnitId)
                    onDone()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Interstitial failed to show: ${adError.message}")
                    interstitialCache[usedUnitId] = null
                    preloadInterstitial(usedUnitId)
                    preloadInterstitial(adUnitId)
                    onDone()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial showed successfully for $featureName")
                }
            }
            targetAd.show(activity)
        } else {
            // If ad not in cache, present the sponsored prompt dialog to the user
            Log.d(TAG, "Ad for $adUnitId not cached yet, presenting sponsored prompt for $featureName")
            preloadInterstitial(adUnitId)
            preloadInterstitial(AdMobUnits.TEST_INTERSTITIAL)
            _pendingPrompt.value = PendingAdPrompt(
                featureName = featureName,
                adUnitId = adUnitId,
                isRewarded = false,
                onConfirm = {
                    _adsShownCount.value += 1
                    _adStatusMessage.value = "Autorización completada para: $featureName"
                    _pendingPrompt.value = null
                    onDone()
                }
            )
        }
    }

    /**
     * Shows a rewarded ad (Prestamevip) to unlock VIP modes or high-power tools.
     */
    fun showRewardedForVip(
        activity: Activity,
        rewardTitle: String = "Herramienta VIP",
        onRewardGranted: () -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        val ad = cachedRewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad dismissed")
                    cachedRewardedAd = null
                    preloadRewarded()
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Rewarded Ad failed to show: ${adError.message}")
                    cachedRewardedAd = null
                    preloadRewarded()
                    _vipUnlocked.value = true
                    _adsShownCount.value += 1
                    onRewardGranted()
                }
            }

            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.type} ${rewardItem.amount}")
                _vipUnlocked.value = true
                _adsShownCount.value += 1
                _adStatusMessage.value = "Recompensa desbloqueada: $rewardTitle"
                onRewardGranted()
            }
        } else {
            // Show prompt to grant reward and trigger background reload
            _pendingPrompt.value = PendingAdPrompt(
                featureName = rewardTitle,
                adUnitId = AdMobUnits.REWARDED_PRESTAMEVIP,
                isRewarded = true,
                onConfirm = {
                    _vipUnlocked.value = true
                    _adsShownCount.value += 1
                    _adStatusMessage.value = "Recompensa VIP Activada: $rewardTitle"
                    _pendingPrompt.value = null
                    preloadRewarded()
                    onRewardGranted()
                }
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AdMobManager? = null

        fun getInstance(context: Context): AdMobManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdMobManager(context.applicationContext).also {
                    INSTANCE = it
                    it.initialize()
                }
            }
        }
    }
}
