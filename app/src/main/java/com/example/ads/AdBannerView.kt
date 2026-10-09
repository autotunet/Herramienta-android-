package com.example.ads

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.BorderDim
import com.example.ui.theme.LocalAccentColor
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdBannerView(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobUnits.BANNER_VIP,
    showBorder: Boolean = true
) {
    val accent = LocalAccentColor.current
    var isAdLoaded by remember { mutableStateOf(false) }
    var currentUnitId by remember { mutableStateOf(adUnitId) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xE6080D1A))
            .then(
                if (showBorder) {
                    Modifier.border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                BorderDim,
                                accent.copy(alpha = 0.5f),
                                BorderDim
                            )
                        ),
                        RoundedCornerShape(8.dp)
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚡ ESPACIO PUBLICITARIO VIP • GOOGLE ADMOB",
                color = accent.copy(alpha = 0.8f),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = currentUnitId
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                isAdLoaded = true
                                Log.d("AdBannerView", "Ad loaded for: $currentUnitId")
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                super.onAdFailedToLoad(error)
                                Log.w("AdBannerView", "Ad failed: ${error.message} (code ${error.code})")
                                // If live ad fails due to no fill (code 3), switch to test unit for UI display in dev/emulator
                                if (error.code == AdRequest.ERROR_CODE_NO_FILL && currentUnitId != AdMobUnits.TEST_BANNER) {
                                    currentUnitId = AdMobUnits.TEST_BANNER
                                    loadAd(AdRequest.Builder().build())
                                }
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                },
                update = { adView ->
                    // View update if needed
                }
            )
        }
    }
}
